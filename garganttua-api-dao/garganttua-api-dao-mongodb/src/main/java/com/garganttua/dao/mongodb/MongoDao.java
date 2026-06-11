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

		List<Object> results = new ArrayList<>();
		for (Document doc : iterable) {
			results.add(documentToDto(doc));
		}
		return results;
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
						map.put(field.getName(), value);
					}
				}
				clazz = clazz.getSuperclass();
			}
			return new Document(map);
		} catch (IllegalAccessException e) {
			throw new ApiException("Failed to convert DTO to MongoDB Document", e);
		}
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
		if (!doc.containsKey(fieldName)) {
			return;
		}
		field.setAccessible(true);
		Object value = doc.get(fieldName);
		if (comps.containsKey(fieldName)) {
			field.set(instance, resolveReference(field, value));
		} else if (!isReference(value)) {
			// A reference reaching a non-composition field means we are one level too deep
			// (a composed DTO that itself composes): leave it null rather than mis-set it.
			field.set(instance, value);
		}
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