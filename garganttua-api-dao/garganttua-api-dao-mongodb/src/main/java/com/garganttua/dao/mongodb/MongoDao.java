package com.garganttua.dao.mongodb;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.api.commons.sort.SortDirection;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.annotations.Reflected;
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

	private final MongoDatabase database;
	private final String collectionName;
	private IClass<?> dtoClass;

	public MongoDao(MongoDatabase database, String collectionName) {
		this.database = database;
		this.collectionName = collectionName;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void registerDomain(IDomainDefinition domainDefinition) {
		if (domainDefinition != null && domainDefinition.dtoDefinitions() != null && !domainDefinition.dtoDefinitions().isEmpty()) {
			this.dtoClass = ((com.garganttua.api.commons.definition.IDtoDefinition<?>) domainDefinition.dtoDefinitions().get(0)).dtoClass();
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

	private Document dtoToDocument(Object dto) throws ApiException {
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
					if (value != null) {
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

	private Object documentToDto(Document doc) throws ApiException {
		try {
			Object instance = dtoClass.getDeclaredConstructor().newInstance();
			IClass<?> clazz = dtoClass;
			while (clazz != null) {
				for (IField field : clazz.getDeclaredFields()) {
					int mods = field.getModifiers();
					if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
						continue;
					}
					String fieldName = field.getName();
					if (doc.containsKey(fieldName)) {
						// TODO Utilise le reflection() pour interagir avec les champs plutôt que de faire du brute force
						field.setAccessible(true);
						field.set(instance, doc.get(fieldName));
					}
				}
				clazz = clazz.getSuperclass();
			}
			return instance;
		} catch (Exception e) {
			throw new ApiException("Failed to convert MongoDB Document to DTO", e);
		}
	}
}