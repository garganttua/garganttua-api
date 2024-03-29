package com.garganttua.api.core.objects.fields.accessors;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.fields.GGAPIFields;
import com.garganttua.api.core.objects.fields.GGAPIFieldsException;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelper;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelperExcpetion;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIObjectFieldSetter {

	private Class<?> clazz;
	private List<Object> fields;
	private GGAPIObjectAddress address;

	public GGAPIObjectFieldSetter(Class<?> clazz, List<Object> fields, GGAPIObjectAddress address)
			throws GGAPIObjectQueryException {
		if (clazz == null) {
			throw new GGAPIObjectQueryException("class is null");
		}
		if (fields == null) {
			throw new GGAPIObjectQueryException("fields is null");
		}
		if (address == null) {
			throw new GGAPIObjectQueryException("address is null");
		}

		this.clazz = clazz;
		this.fields = fields;
		this.address = address;
	}

	public Object setValue(Object fieldValue) throws GGAPIObjectQueryException {
		if (log.isDebugEnabled()) {
			log.debug("Setting value of object : class {}, address {}", this.clazz, this.address);
		}
		try {
			return this.setValue(GGAPIObjectReflectionHelper.instanciateNewObject(clazz), fieldValue);
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIObjectQueryException(e);
		}

	}

	public Object setValue(Object object, Object value) throws GGAPIObjectQueryException {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Setting value of object : {}, value {}, class {}, address {}", object, value, this.clazz,
						this.address);
			}
			if (object == null) {
				throw new GGAPIObjectQueryException("object is null");
			}
			if( !object.getClass().isAssignableFrom(this.clazz) ) {
				throw new GGAPIObjectQueryException("object is not of type "+this.clazz);
			}

			if (this.fields.size() == 1) {
				Field field = (Field) this.fields.get(0);
				String fieldName = this.address.getElement(0);
				if (!field.getName().equals(fieldName)) {
					throw new GGAPIObjectQueryException("field names of address " + fieldName + " and fields list "
							+ field.getName() + " do not match");
				}
				GGAPIObjectReflectionHelper.setObjectFieldValue(object, field, value);
			} else {
				this.setValueRecursively(object, value, 0, 0);
			}
			return object;
		} catch (IllegalArgumentException | GGAPIObjectReflectionHelperExcpetion | GGAPIFieldsException e) {
			throw new GGAPIObjectQueryException(e);
		}
	}

	private void setValueRecursively(Object object, Object value, int fieldIndex, int fieldNameIndex)
			throws GGAPIObjectQueryException, GGAPIObjectReflectionHelperExcpetion, GGAPIFieldsException {
		if (log.isDebugEnabled()) {
			log.debug(
					"Setting value recursively of object : {}, value {}, class {}, address {}, fieldIndex {}, fieldNameIndex {}",
					object, value, this.clazz, this.address, fieldIndex, fieldNameIndex);
		}
		boolean isLastIteration = (fieldIndex + 2 == this.fields.size());
		Field field = (Field) this.fields.get(fieldIndex);
		Field nextField = (Field) this.fields.get(fieldIndex + 1);
		String fieldName = this.address.getElement(fieldNameIndex);

		if (!field.getName().equals(fieldName)) {
			throw new GGAPIObjectQueryException(
					"field names of address " + fieldName + " and fields list " + field.getName() + " do not match");
		}

		Object temp = GGAPIObjectReflectionHelper.getObjectFieldValue(object, fieldName, field);
		if (temp == null ) {
			temp = GGAPIFields.instanciate(field);
			GGAPIObjectReflectionHelper.setObjectFieldValue(object, field, temp);
		}

		if (GGAPIFields.isArrayOrMapOrCollectionField(field)) {
			this.doIfIsCollection(object, value, fieldIndex, fieldNameIndex, isLastIteration, field, nextField, temp);
			this.doIfIsArray(object, value, fieldIndex, fieldNameIndex, isLastIteration, field, nextField, temp);
			this.doIfIsMap(object, value, fieldIndex, fieldNameIndex, isLastIteration, field, nextField, temp);
		} else {
			if (isLastIteration) {
				GGAPIObjectReflectionHelper.setObjectFieldValue(temp, nextField, value);
			} else {
				this.setValueRecursively(temp, value, fieldIndex + 1, fieldNameIndex + 1);
			}
		}
	}
	
	private void doIfIsMap(Object object, Object value, int fieldIndex, int fieldNameIndex,
			boolean isLastIteration, Field field, Field nextField, Object temp)
			throws GGAPIObjectReflectionHelperExcpetion, GGAPIObjectQueryException, GGAPIFieldsException {
		if (Map.class.isAssignableFrom(field.getType())) {
			
			Map<?,?> collectionTarget = (Map<?,?>) temp;
			List<?> collectionSource = ((List<?>) value);

			if (collectionSource.size() != collectionTarget.size()) {
				int nbToCreate = collectionSource.size() - collectionTarget.size();
				if (nbToCreate > 0) {
					Class<?> keyType = GGAPIFields.getGenericType(field, 0);
					Class<?> valueType = GGAPIFields.getGenericType(field, 1);
					for (int i = 0; i < nbToCreate; i++) {
						collectionTarget.put(GGAPIObjectReflectionHelper.instanciateNewObject(keyType), GGAPIObjectReflectionHelper.instanciateNewObject(valueType));
					}
				}
			}
			
			String nextFieldName = this.address.getElement(fieldNameIndex+1);
			Iterator<?> it = null;
			if( nextFieldName.equals(GGAPIObjectAddress.MAP_KEY_INDICATOR) ) {
				it = collectionTarget.keySet().iterator();
			}
			if( nextFieldName.equals(GGAPIObjectAddress.MAP_VALUE_INDICATOR) ) {
				it = collectionTarget.values().iterator();
			}
			if( it == null ) {
				throw new GGAPIObjectQueryException("Invalid address, "+nextFieldName+" should be either #key or #value");
			}
			for (int i = 0; i < collectionSource.size(); i++) {
				Object tempObject = it.next();
				if (isLastIteration) {
					GGAPIObjectReflectionHelper.setObjectFieldValue(tempObject, nextField, collectionSource.get(i));
				} else {
					this.setValueRecursively(tempObject, collectionSource.get(i), fieldIndex + 1, fieldNameIndex + 2);
				}
			}
		}
	}

	private void doIfIsArray(Object object, Object value, int fieldIndex, int fieldNameIndex, boolean isLastIteration,
			Field field, Field nextField, Object temp)
			throws GGAPIObjectReflectionHelperExcpetion, GGAPIObjectQueryException, GGAPIFieldsException {
		if (field.getType().isArray()) {
			if (field.getType().getComponentType().isArray()) {
				this.handleMultiDimensionalArray(object, value, fieldIndex, fieldNameIndex, isLastIteration, field,
						nextField, value);
			} else {
				Object[] collectionTarget = (Object[]) temp;
				List<?> collectionSource = ((List<?>) value);

				Class<?> listObjectType = field.getType().getComponentType();

				if (collectionSource.size() != collectionTarget.length) {
					int nbToCreate = collectionSource.size() - collectionTarget.length;
					if (nbToCreate > 0) {
						collectionTarget = Arrays.copyOf(collectionTarget, collectionTarget.length + nbToCreate);
					}
					for (int i = 0; i < nbToCreate; i++) {
						collectionTarget[collectionTarget.length + i - 1] = GGAPIObjectReflectionHelper
								.instanciateNewObject(listObjectType);
					}

					GGAPIObjectReflectionHelper.setObjectFieldValue(object, field, collectionTarget);
				}

				for (int i = 0; i < collectionSource.size(); i++) {
					Object tempObject = collectionTarget[i];
					if (isLastIteration) {
						GGAPIObjectReflectionHelper.setObjectFieldValue(tempObject, nextField, collectionSource.get(i));
					} else {
						this.setValueRecursively(tempObject, collectionSource.get(i), fieldIndex + 1, fieldNameIndex + 1);
					}
				}
			}
		}
	}

	private void doIfIsCollection(Object object, Object value, int fieldIndex, int fieldNameIndex,
			boolean isLastIteration, Field field, Field nextField, Object temp)
			throws GGAPIObjectReflectionHelperExcpetion, GGAPIObjectQueryException, GGAPIFieldsException {
		if (Collection.class.isAssignableFrom(field.getType())) {
			
			Collection<?> collectionTarget = (Collection<?>) temp;
			List<?> collectionSource = ((List<?>) value);

			if (collectionSource.size() != collectionTarget.size()) {
				int nbToCreate = collectionSource.size() - collectionTarget.size();
				if (nbToCreate > 0) {
					Class<?> listObjectType = GGAPIFields.getGenericType(field, 0);
					for (int i = 0; i < nbToCreate; i++) {
						collectionTarget.add(GGAPIObjectReflectionHelper.instanciateNewObject(listObjectType));
					}
				}
			}
			Iterator<?> it = collectionTarget.iterator();
			for (int i = 0; i < collectionSource.size(); i++) {
				Object tempObject = it.next();
				if (isLastIteration) {
					GGAPIObjectReflectionHelper.setObjectFieldValue(tempObject, nextField, collectionSource.get(i));
				} else {
					this.setValueRecursively(tempObject, collectionSource.get(i), fieldIndex + 1, fieldNameIndex + 1);
				}
			}
		}
	}

	private void handleMultiDimensionalArray(Object object, Object value, int fieldIndex, int fieldNameIndex,
			boolean isLastIteration, Field field, Field nextField, Object fieldValue)
			throws GGAPIObjectReflectionHelperExcpetion, GGAPIObjectQueryException {
		Object[] array = (Object[]) fieldValue;
		Object[] sourceArray = (Object[]) value;

		if (array == null || array.length != sourceArray.length) {
			Class<?> componentType = field.getType().getComponentType();
			array = (Object[]) Array.newInstance(componentType, sourceArray.length);
			GGAPIObjectReflectionHelper.setObjectFieldValue(object, field, array);
		}

		for (int i = 0; i < sourceArray.length; i++) {
			Object subArray = array[i];
			if (subArray == null) {
				Class<?> componentType = field.getType().getComponentType();
				subArray = Array.newInstance(componentType, sourceArray.length);
				array[i] = subArray;
			}
			if (isLastIteration) {
				Array.set(subArray, i, sourceArray[i]);
			} else {
				handleMultiDimensionalArray(subArray, sourceArray[i], fieldIndex + 1, fieldNameIndex + 1,
						isLastIteration, field, nextField, fieldValue);
			}
		}
	}

}
