package com.garganttua.dao.mongodb;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.definition.DtoComposition;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.api.commons.sort.SortDirection;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.annotations.Reflected;
import com.mongodb.DBRef;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;

@Reflected
public class MongoDao implements IDao {

	private static final String MONGO_ID = "_id";
	private static final String DEFAULT_UUID_FIELD = "uuid";

	private final MongoDatabase database;
	private final String collectionName;
	private IClass<?> dtoClass;

	/** Field name holding the DTO uuid — also the {@code $id} carried by every DBRef this DAO emits. */
	private String uuidFieldName = DEFAULT_UUID_FIELD;

	/** Composition field name → target collection ({@code $ref}), as declared by {@code @Composed}/{@code .composed(...)}. */
	private final Map<String, String> compositions = new HashMap<>();

	public MongoDao(MongoDatabase database, String collectionName) {
		this.database = database;
		this.collectionName = collectionName;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void registerDomain(IDomainDefinition domainDefinition) {
		if (domainDefinition != null && domainDefinition.dtoDefinitions() != null && !domainDefinition.dtoDefinitions().isEmpty()) {
			IDtoDefinition<?> dtoDefinition = (IDtoDefinition<?>) domainDefinition.dtoDefinitions().get(0);
			this.dtoClass = dtoDefinition.dtoClass();
			if (dtoDefinition.uuid() != null) {
				this.uuidFieldName = dtoDefinition.uuid().getLastElement();
			}
			this.compositions.clear();
			for (DtoComposition composition : dtoDefinition.compositions()) {
				this.compositions.put(composition.field().getLastElement(), composition.collection());
			}
		}
	}

	@Override
	public List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort)
			throws ApiException {
		return find(pageable, filter, sort, Optional.empty());
	}

	@Override
	public List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort,
			Optional<List<String>> projection) throws ApiException {
		Bson mongoFilter = filter.map(MongoFilterConverter::convert).orElse(new Document());

		FindIterable<Document> iterable = getCollection().find(mongoFilter);

		sort.ifPresent(s -> {
			Bson mongoSort = s.getDirection() == SortDirection.asc
					? Sorts.ascending(s.getFieldName())
					: Sorts.descending(s.getFieldName());
			iterable.sort(mongoSort);
		});

		pageable.ifPresent(p -> {
			iterable.skip(p.getPageIndex() * p.getPageSize());
			iterable.limit(p.getPageSize());
		});

		applyProjection(iterable, projection);

		List<Object> results = new ArrayList<>();
		for (Document doc : iterable) {
			results.add(documentToDto(doc));
		}
		return results;
	}

	/**
	 * Narrows the fetched fields to the requested projection (entity field names translated to their
	 * document fields) for IO savings. Always force-includes the uuid field and every composition
	 * (DBRef) field — {@code _id} is returned by default — so {@link #documentToDto} can still map the
	 * identity and resolve references. No-op when the projection is empty.
	 */
	private void applyProjection(FindIterable<Document> iterable, Optional<List<String>> projection) {
		if (projection == null || projection.isEmpty() || projection.get().isEmpty()) {
			return;
		}
		java.util.LinkedHashSet<String> docFields = new java.util.LinkedHashSet<>();
		for (String entityField : projection.get()) {
			if (entityField == null || entityField.isBlank()) {
				continue;
			}
			// A dotted path a.b projects its top-level document field (Mongo returns the whole sub-doc).
			String head = entityField.contains(".")
					? entityField.substring(0, entityField.indexOf('.'))
					: entityField;
			docFields.add(translateToDtoField(head.trim()));
		}
		if (docFields.isEmpty()) {
			return;
		}
		docFields.add(this.uuidFieldName);
		docFields.addAll(this.compositions.keySet());
		iterable.projection(Projections.include(new ArrayList<>(docFields)));
	}

	/**
	 * Translates an ENTITY field name to its document (DTO) field name by reading {@code @FieldMappingRule}
	 * on the DTO fields ({@code sourceFieldAddress} = the entity field). Falls back to the same name when
	 * no rule maps it (DTO field name == entity field name).
	 */
	private String translateToDtoField(String entityField) {
		if (this.dtoClass == null) {
			return entityField;
		}
		IClass<?> current = this.dtoClass;
		while (current != null) {
			for (IField field : current.getDeclaredFields()) {
				com.garganttua.core.mapper.annotations.FieldMappingRule[] rules =
						field.getAnnotationsByType(IClass.getClass(com.garganttua.core.mapper.annotations.FieldMappingRule.class));
				for (com.garganttua.core.mapper.annotations.FieldMappingRule rule : rules) {
					if (entityField.equals(rule.sourceFieldAddress())) {
						return field.getName();
					}
				}
			}
			current = current.getSuperclass();
		}
		return entityField;
	}

	@Override
	public Object save(Object object) throws ApiException {
		Document doc = dtoToDocument(object);
		Object id = doc.get(MONGO_ID);

		if (id != null) {
			getCollection().replaceOne(
					Filters.eq(MONGO_ID, id),
					doc,
					new ReplaceOptions().upsert(true));
		} else {
			getCollection().insertOne(doc);
		}

		return object;
	}

	@Override
	public void delete(Object object) throws ApiException {
		Document doc = dtoToDocument(object);
		Object id = doc.get(MONGO_ID);

		if (id == null) {
			throw new ApiException("Cannot delete document without _id");
		}

		DeleteResult result = getCollection().deleteOne(Filters.eq(MONGO_ID, id));
		if (result.getDeletedCount() == 0) {
			throw new ApiException("Document not found for deletion: _id=" + id);
		}
	}

	@Override
	public long count(IFilter filter) throws ApiException {
		if (filter == null) {
			return getCollection().countDocuments();
		}
		Bson mongoFilter = MongoFilterConverter.convert(filter);
		return getCollection().countDocuments(mongoFilter);
	}

	private MongoCollection<Document> getCollection() {
		return this.database.getCollection(this.collectionName);
	}

	Document dtoToDocument(Object dto) throws ApiException {
		try {
			Map<String, Object> map = new LinkedHashMap<>();
			IClass<?> clazz = dtoClass;
			while (clazz != null) {
				for (IField field : clazz.getDeclaredFields()) {
					int mods = field.getModifiers();
					if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
						continue;
					}
					field.setAccessible(true);
					Object value = field.get(dto);
					if (value == null) {
						continue;
					}
					String collection = this.compositions.get(field.getName());
					if (collection != null) {
						map.put(field.getName(), toReference(value, collection));
					} else {
						map.put(field.getName(), toStorable(value));
					}
				}
				clazz = clazz.getSuperclass();
			}
			// Project the domain uuid onto _id so save() upserts (rather than always inserting →
			// duplicate rows) and delete() has a key. The uuid is kept under its own field name so
			// uuid-keyed filters (readOne / readAll by uuid) still match — _id is an added projection.
			Object uuidValue = map.get(this.uuidFieldName);
			if (uuidValue != null) {
				map.put(MONGO_ID, uuidValue);
			}
			return new Document(map);
		} catch (IllegalAccessException e) {
			throw new ApiException("Failed to convert DTO to MongoDB Document", e);
		}
	}

	/**
	 * Normalises a plain (non-composition) field value into a BSON-friendly form before storage.
	 * An {@code enum} is written as its {@code name()} so the wire shape is codec-independent and
	 * human-readable; the read side rebuilds it from the field's declared enum type. Everything else
	 * (the temporal types, numbers, strings) is handed to the driver verbatim.
	 */
	private Object toStorable(Object value) {
		if (value instanceof Enum<?> e) {
			return e.name();
		}
		if (value instanceof com.garganttua.core.crypto.IKey key) {
			// No BSON codec exists for IKey — persist it as a self-describing sub-document.
			return IKeyBsonBridge.toDocument(key);
		}
		return value;
	}

	/**
	 * Turns a composition field value into the reference(s) actually persisted: a single
	 * {@link DBRef} for a 1-1 field, or a {@code List<DBRef>} for a 1-N ({@link Collection}) field.
	 * Only the reference is stored — the composed DTO itself lives in its own collection.
	 */
	private Object toReference(Object value, String collection) throws ApiException {
		if (value instanceof Collection<?> elements) {
			List<DBRef> refs = new ArrayList<>(elements.size());
			for (Object element : elements) {
				if (element != null) {
					refs.add(new DBRef(collection, uuidOf(element)));
				}
			}
			return refs;
		}
		return new DBRef(collection, uuidOf(value));
	}

	/** Reads the uuid carried by a composed DTO — it becomes the {@code $id} of its DBRef. */
	private Object uuidOf(Object dto) throws ApiException {
		IClass<?> clazz = IClass.getClass(dto.getClass());
		while (clazz != null) {
			try {
				IField field = clazz.getDeclaredField(this.uuidFieldName);
				field.setAccessible(true);
				return field.get(dto);
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
			} catch (IllegalAccessException e) {
				throw new ApiException("Failed to read uuid field '" + this.uuidFieldName + "' on composed DTO "
						+ dto.getClass().getName(), e);
			}
		}
		throw new ApiException("Composed DTO " + dto.getClass().getName() + " has no uuid field '"
				+ this.uuidFieldName + "' to reference");
	}

	private Object documentToDto(Document doc) throws ApiException {
		return documentToDto(doc, dtoClass, this.compositions);
	}

	/**
	 * Maps a Mongo {@link Document} onto a fresh instance of {@code clazz}. The {@code compositions}
	 * map drives DBRef resolution: each composition field is eagerly resolved one level deep — the
	 * referenced document(s) are read and mapped with NO further composition resolution (anti-cycle),
	 * so a graph of references can never loop.
	 */
	private Object documentToDto(Document doc, IClass<?> clazz, Map<String, String> comps) throws ApiException {
		try {
			Object instance = clazz.getDeclaredConstructor().newInstance();
			IClass<?> current = clazz;
			while (current != null) {
				for (IField field : current.getDeclaredFields()) {
					applyField(instance, field, doc, comps);
				}
				current = current.getSuperclass();
			}
			return instance;
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Failed to convert MongoDB Document to DTO", e);
		}
	}

	/** Maps a single document field onto {@code instance}, resolving DBRefs for composition fields. */
	private void applyField(Object instance, IField field, Document doc, Map<String, String> comps)
			throws ApiException, IllegalAccessException {
		int mods = field.getModifiers();
		if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
			return;
		}
		String fieldName = field.getName();
		Object value;
		if (doc.containsKey(fieldName)) {
			value = doc.get(fieldName);
		} else if (fieldName.equals(this.uuidFieldName) && doc.containsKey(MONGO_ID)) {
			// The uuid is projected onto _id on write; recover it from there for documents that
			// carry only _id (e.g. written by another tool, or an _id-only projection).
			value = doc.get(MONGO_ID);
		} else {
			return;
		}
		field.setAccessible(true);
		if (comps.containsKey(fieldName)) {
			field.set(instance, resolveReference(field, value));
		} else if (!isReference(value)) {
			// A reference reaching a non-composition field means we are one level too deep
			// (a composed DTO that itself composes): leave it null rather than mis-set it.
			Class<?> target = rawType(field);
			Object coerced = coerce(value, target);
			try {
				field.set(instance, coerced);
			} catch (IllegalArgumentException e) {
				throw new ApiException("Cannot map field '" + fieldName + "' of " + field.getDeclaringClass().getName()
						+ ": stored " + describeType(value) + " is not assignable to declared "
						+ (target == null ? "type" : target.getName()), e);
			}
		}
	}

	/** The declared raw type of a non-generic field, or {@code null} when it is parameterized (e.g. a {@code List<X>}). */
	private Class<?> rawType(IField field) {
		Type generic = field.getGenericType();
		return generic instanceof Class<?> raw ? raw : null;
	}

	private String describeType(Object value) {
		return value == null ? "null" : value.getClass().getName();
	}

	/**
	 * Adapts a value decoded from BSON to the field's declared Java type — MongoDB's Document codec
	 * is lossy across the JVM type system (an enum comes back as a String, a {@code java.time.Instant}
	 * as a {@code java.util.Date}, a 32-bit field as an {@code Integer}). Handles the common, lossless
	 * cases; anything it does not recognise is returned untouched for {@code field.set} to accept or reject.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object coerce(Object value, Class<?> target) throws ApiException {
		// A persisted IKey sub-document is reconstructed regardless of the (interface) target type.
		if (IKeyBsonBridge.isKeyDocument(value)) {
			return IKeyBsonBridge.fromDocument((Document) value);
		}
		if (value == null || target == null || target.isInstance(value)) {
			return value;
		}
		if (value instanceof org.bson.types.Binary binary && target == byte[].class) {
			// The driver decodes BSON binary back as org.bson.types.Binary, not byte[] — unwrap it,
			// else any byte[] field (token signature, key material) is unreadable.
			return binary.getData();
		}
		if (target.isEnum() && value instanceof String name) {
			return Enum.valueOf((Class<? extends Enum>) target, name);
		}
		if (value instanceof java.util.Date date) {
			return fromDate(date, target);
		}
		if (value instanceof Number number) {
			return fromNumber(number, target);
		}
		if (value instanceof String text) {
			return fromString(text, target);
		}
		return value;
	}

	/** {@code java.util.Date} (how the driver decodes a BSON datetime) → the declared {@code java.time} type, at UTC. */
	private Object fromDate(java.util.Date date, Class<?> target) {
		java.time.Instant instant = date.toInstant();
		if (target == java.time.Instant.class) {
			return instant;
		}
		if (target == java.time.LocalDateTime.class) {
			return java.time.LocalDateTime.ofInstant(instant, java.time.ZoneOffset.UTC);
		}
		if (target == java.time.LocalDate.class) {
			return java.time.LocalDate.ofInstant(instant, java.time.ZoneOffset.UTC);
		}
		if (target == java.time.ZonedDateTime.class) {
			return instant.atZone(java.time.ZoneOffset.UTC);
		}
		if (target == java.time.OffsetDateTime.class) {
			return instant.atOffset(java.time.ZoneOffset.UTC);
		}
		return date;
	}

	/** Widens/narrows a stored {@link Number} to the declared numeric type (handles primitives too). */
	private Object fromNumber(Number number, Class<?> target) {
		if (target == Long.class || target == long.class) {
			return number.longValue();
		}
		if (target == Integer.class || target == int.class) {
			return number.intValue();
		}
		if (target == Double.class || target == double.class) {
			return number.doubleValue();
		}
		if (target == Float.class || target == float.class) {
			return number.floatValue();
		}
		if (target == Short.class || target == short.class) {
			return number.shortValue();
		}
		if (target == Byte.class || target == byte.class) {
			return number.byteValue();
		}
		return number;
	}

	/** Parses a stored {@code String} into the declared scalar type (configs occasionally land as text). */
	private Object fromString(String text, Class<?> target) {
		if (target == Integer.class || target == int.class) {
			return Integer.valueOf(text);
		}
		if (target == Long.class || target == long.class) {
			return Long.valueOf(text);
		}
		if (target == Double.class || target == double.class) {
			return Double.valueOf(text);
		}
		if (target == Float.class || target == float.class) {
			return Float.valueOf(text);
		}
		if (target == Boolean.class || target == boolean.class) {
			return Boolean.valueOf(text);
		}
		return text;
	}

	private boolean isReference(Object value) {
		if (value instanceof DBRef) {
			return true;
		}
		return value instanceof Collection<?> c && !c.isEmpty() && c.iterator().next() instanceof DBRef;
	}

	/**
	 * Resolves a composition field's stored reference(s) into the composed DTO(s): a single
	 * {@link DBRef} → one DTO ({@code field}'s type); a {@code List<DBRef>} → a {@code List} of DTOs
	 * (the field's element type). Each referenced document is fetched from its collection by uuid.
	 */
	private Object resolveReference(IField field, Object value) throws ApiException {
		if (value instanceof Collection<?> refs) {
			IClass<?> elementType = listElementType(field);
			List<Object> resolved = new ArrayList<>(refs.size());
			for (Object ref : refs) {
				if (ref instanceof DBRef dbRef) {
					Object dto = resolveOne(dbRef, elementType);
					if (dto != null) {
						resolved.add(dto);
					}
				}
			}
			return resolved;
		}
		if (value instanceof DBRef dbRef) {
			return resolveOne(dbRef, field.getType());
		}
		return null;
	}

	/** Fetches the document referenced by {@code dbRef} and maps it (one level deep, no nested resolution). */
	private Object resolveOne(DBRef dbRef, IClass<?> targetType) throws ApiException {
		Document referenced = this.database.getCollection(dbRef.getCollectionName())
				.find(Filters.eq(this.uuidFieldName, dbRef.getId()))
				.first();
		if (referenced == null) {
			return null;
		}
		return documentToDto(referenced, targetType, Map.of());
	}

	/** The declared element type of a {@code List<X>} composition field, or {@code Object} if not parameterized. */
	private IClass<?> listElementType(IField field) {
		Type generic = field.getGenericType();
		if (generic instanceof ParameterizedType parameterized) {
			Type[] args = parameterized.getActualTypeArguments();
			if (args.length == 1 && args[0] instanceof Class<?> elementClass) {
				return IClass.getClass(elementClass);
			}
		}
		return IClass.getClass(Object.class);
	}
}