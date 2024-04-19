package com.garganttua.api.core.objects.query;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.exceptions.GGAPICoreExceptionCode;
import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.GGAPIObjectAddressException;
import com.garganttua.api.core.objects.fields.GGAPIFields;
import com.garganttua.api.core.objects.fields.GGAPIFieldsException;
import com.garganttua.api.core.objects.fields.accessors.GGAPIObjectFieldGetter;
import com.garganttua.api.core.objects.fields.accessors.GGAPIObjectFieldSetter;
import com.garganttua.api.core.objects.methods.GGAPIObjectMethodInvoker;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelper;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelperExcpetion;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIObjectQuery implements IGGAPIObjectQuery {
	
	private Class<?> objectClass;
	private Object object;

	protected GGAPIObjectQuery(Class<?> objectClass) throws GGAPIObjectQueryException {
		if( objectClass == null ) {
			throw new GGAPIObjectQueryException(GGAPICoreExceptionCode.UNKNOWN_ERROR, "class is null");
		}
		this.objectClass = objectClass;
		try {
			this.object = GGAPIObjectReflectionHelper.instanciateNewObject(objectClass);
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIObjectQueryException(e);
		}
	}
	
	protected GGAPIObjectQuery(Object object) throws GGAPIObjectQueryException {
		if( object == null ) {
			throw new GGAPIObjectQueryException(GGAPICoreExceptionCode.UNKNOWN_ERROR, "object is null");
		}
		this.object = object;
		this.objectClass = object.getClass();
	}
	
	protected GGAPIObjectQuery(Class<?> objectClass, Object object) throws GGAPIObjectQueryException {
		if( object == null ) {
			throw new GGAPIObjectQueryException(GGAPICoreExceptionCode.UNKNOWN_ERROR, "object is null");
		}
		if( objectClass == null ) {
			throw new GGAPIObjectQueryException(GGAPICoreExceptionCode.UNKNOWN_ERROR, "class is null");
		}
		if( this.object.getClass().isAssignableFrom(this.objectClass) ) {
			throw new GGAPIObjectQueryException(GGAPICoreExceptionCode.UNKNOWN_ERROR, "Provided class "+objectClass+" and object "+object.getClass()+" do not match");
		}
		this.object = object;
		this.objectClass = objectClass;
	}

	@Override
	public List<Object> find(String fieldAddress) throws GGAPIObjectQueryException {
		try {
			return this.find(new GGAPIObjectAddress(fieldAddress));
		} catch (GGAPIObjectAddressException e) {
			throw new GGAPIObjectQueryException(e);
		}
	}

	@Override
	public List<Object> find(GGAPIObjectAddress fieldAddress)
			throws GGAPIObjectQueryException {
		if (log.isDebugEnabled()) {
			log.debug("Looking for field " + fieldAddress + " in " + objectClass);
		}
		List<Object> list = new ArrayList<Object>();
		return this.findRecursively(this.objectClass, fieldAddress, 0, list);
	}

	private List<Object> findRecursively(Class<?> clazz, GGAPIObjectAddress address, int index,
			List<Object> list) throws GGAPIObjectQueryException {
		if (log.isDebugEnabled()) {
			log.debug("Looking for object element " + address.getElement(index) + " in " + clazz);
		}

		if (clazz == null || index >= address.length()) {
			throw new GGAPIObjectQueryException(GGAPICoreExceptionCode.OBJECT_NOT_FOUND,
					"Object element " + address.getElement(index) + " not found in class " + clazz);
		}

		Field field = GGAPIObjectReflectionHelper.getField(clazz, address.getElement(index));

		Method method = null;
		if (index == address.length() - 1 && field == null) {
			method = GGAPIObjectReflectionHelper.getMethod(clazz, address.getElement(index));
		}

		if (field == null && method == null) {
			if (clazz.getSuperclass() != null) {
				return this.findRecursively(clazz.getSuperclass(), address, index, list);
			}
		} else if (field != null && method == null) {
			list.add(field);
			if (index == address.length() - 1) {
				return list;
			} else {
				Class<?> fieldType = field.getType();
				if (Collection.class.isAssignableFrom(fieldType)) {
					Class<?> genericType = GGAPIFields.getGenericType(field, 0);
					return this.findRecursively(genericType, address, index + 1, list);
				} else if (Map.class.isAssignableFrom(fieldType)) {

					if (address.getElement(index + 1).equals(GGAPIObjectAddress.MAP_VALUE_INDICATOR)) {
						Class<?> genericType = GGAPIFields.getGenericType(field, 1);
						return this.findRecursively(genericType, address, index + 2, list);
					} else if (address.getElement(index + 1).equals(GGAPIObjectAddress.MAP_KEY_INDICATOR)) {
						Class<?> genericType = GGAPIFields.getGenericType(field, 0);
						return this.findRecursively(genericType, address, index + 2, list);
					} else {
						throw new GGAPIObjectQueryException(GGAPICoreExceptionCode.VALUE_OR_KEY_NOT_IN_ADDRESS, "Field " + address.getElement(index)
								+ " is a map, so address must indicate key or value");
					}

				} else {
					return this.findRecursively(field.getType(), address, index + 1, list);
				}
			}
		} else if (field == null && method != null) {
			list.add(method);
			return list;
		} else if (field != null && method != null) {
			throw new GGAPIObjectQueryException(GGAPICoreExceptionCode.FIELD_METHOD_SAME_NAME, 
					"Object element " + address.getElement(index) + " is also a field and a method in " + clazz);
		}

		throw new GGAPIObjectQueryException (GGAPICoreExceptionCode.OBJECT_NOT_FOUND, 
				"Object element " + address.getElement(index) + " not found in class " + clazz);
	}

	@Override
	public GGAPIObjectAddress address(String elementName) throws GGAPIObjectQueryException {
		if (log.isDebugEnabled()) {
			log.debug("Looking for object element " + elementName + " in " + objectClass);
		}
		return this.address(this.objectClass, elementName, null);
	}

	private GGAPIObjectAddress address(Class<?> objectClass, String elementName, GGAPIObjectAddress address) throws GGAPIObjectQueryException {
		if (log.isDebugEnabled()) {
			log.debug("Looking for object element " + elementName + " in " + objectClass + " address " + address);
		}
		Field field = null;
		try {
			field = objectClass.getDeclaredField(elementName);
		} catch (NoSuchFieldException | SecurityException e) {
			
		}

		Method method = GGAPIObjectReflectionHelper.getMethod(objectClass, elementName);

		try {
			if (method != null) {
				return new GGAPIObjectAddress(address==null?elementName:address + "." + elementName);
			}
			if (field != null) {
				return new GGAPIObjectAddress(address==null?elementName:address + "." + elementName);
			}
		} catch (GGAPIObjectAddressException e) {
			throw new GGAPIObjectQueryException(e);
		}
		if( objectClass.getSuperclass() != null ) {
			address =  this.address(objectClass.getSuperclass(), elementName, address);
			if( address != null ) {
				return address;
			}
		}
		if (field == null && method == null) {

			for (Field f : objectClass.getDeclaredFields()) {
				if (GGAPIFields.isNotPrimitive(f.getType())) {
					try {
						GGAPIObjectAddress a = null;
						a = this.doIfIsCollection(f, elementName, address);
						if( a != null )
							return a;
						a = this.doIfIsMap(f, elementName, address);
						if( a != null )
							return a;
						a = this.doIfIsArray(f, elementName, address);
						if( a != null )
							return a;
						a = this.doIfNotEnum(f, elementName, address);
						if( a != null )
							return a;
					} catch (GGAPIObjectAddressException e) {
						log.warn("Error occured during processing, ignoring", e);
					}
				}
			}
		}
		return null;
	}
	
	private GGAPIObjectAddress doIfIsMap(Field f, String elementName, GGAPIObjectAddress address) throws GGAPIObjectAddressException, GGAPIObjectQueryException {
		if (Map.class.isAssignableFrom(f.getType())) {
			Class<?> keyClass = GGAPIFields.getGenericType(f, 0);
			Class<?> valueClass = GGAPIFields.getGenericType(f, 1);
			if (GGAPIFields.isNotPrimitive(keyClass)) {
				GGAPIObjectAddress keyAddress = null;
				if( address == null ) {
					keyAddress = new GGAPIObjectAddress(f.getName());
					keyAddress.addElement(GGAPIObjectAddress.MAP_KEY_INDICATOR);
				} else {
					keyAddress = address.clone();
					keyAddress.addElement(f.getName());
					keyAddress.addElement(GGAPIObjectAddress.MAP_KEY_INDICATOR);
				}
				GGAPIObjectAddress a = this.address(keyClass, elementName, keyAddress);
				if (a != null) {
					return a;
				}
			}
			if (GGAPIFields.isNotPrimitive(valueClass)) {
				GGAPIObjectAddress valueAddress = null;
				if( address == null ) {
					valueAddress = new GGAPIObjectAddress(f.getName());
					valueAddress.addElement(GGAPIObjectAddress.MAP_VALUE_INDICATOR);
				} else {
					valueAddress = address.clone();
					valueAddress.addElement(f.getName());
					valueAddress.addElement(GGAPIObjectAddress.MAP_VALUE_INDICATOR);
				}
				GGAPIObjectAddress a = this.address(valueClass, elementName, valueAddress);	
				if (a != null) {
					return a;
				}
			}
		}
		return null;
	}
	
	private GGAPIObjectAddress doIfIsArray(Field f, String elementName, GGAPIObjectAddress address) throws GGAPIObjectAddressException, GGAPIObjectQueryException {
	    if (f.getType().isArray()) {
	        final Class<?> componentType = f.getType().getComponentType();
	        final GGAPIObjectAddress newAddress = address == null ? new GGAPIObjectAddress(f.getName())
	                : address.addElement(f.getName());
	        return this.address(componentType, elementName, newAddress);
	    }
	    return null;
	}

	private GGAPIObjectAddress doIfIsCollection(Field f, String elementName, GGAPIObjectAddress address) throws GGAPIObjectAddressException, GGAPIObjectQueryException {
		if (Collection.class.isAssignableFrom(f.getType())) {
			final Class<?> t = GGAPIFields.getGenericType(f, 0);
			final GGAPIObjectAddress a = this.address(t, elementName, address==null ? new GGAPIObjectAddress(f.getName())
					: address.addElement(f.getName()));
			if (a != null) {
				return a;
			}
		} 
		return null;
	}
	
	private GGAPIObjectAddress doIfNotEnum(Field f, String elementName, GGAPIObjectAddress address) throws GGAPIObjectAddressException, GGAPIObjectQueryException {
		if( !f.getType().isEnum() ) {
			GGAPIObjectAddress a = this.address(f.getType(), elementName, address==null ? new GGAPIObjectAddress(f.getName())
					: address.addElement(f.getName()));
			if (a != null) {
				return a;
			}
		}
		return null;
	}

	@Override
	public Object setValue(Object object, String fieldAddress, Object fieldValue) throws GGAPIObjectQueryException {
		if( object == null ) {
			throw new GGAPIObjectQueryException(GGAPICoreExceptionCode.UNKNOWN_ERROR, "Object is null");
		}
		try {
			return this.setValue(object, new GGAPIObjectAddress(fieldAddress), fieldValue);
		} catch (GGAPIObjectAddressException e) {
			throw new GGAPIObjectQueryException(e);
		}
	}

	@Override
	public Object setValue(Object object, GGAPIObjectAddress fieldAddress, Object fieldValue) throws GGAPIObjectQueryException {
		if( object == null ) {
			throw new GGAPIObjectQueryException(GGAPICoreExceptionCode.UNKNOWN_ERROR, "Object is null");
		}
		List<Object> field = this.find(fieldAddress);
		return new GGAPIObjectFieldSetter(object.getClass(), field, fieldAddress).setValue(object, fieldValue);
	}

	@Override
	public Object getValue(Object object, String fieldAddress) throws GGAPIObjectQueryException {
		if( object == null ) {
			throw new GGAPIObjectQueryException(GGAPICoreExceptionCode.UNKNOWN_ERROR, "Object is null");
		}
		try {
			return this.getValue(object, new GGAPIObjectAddress(fieldAddress));
		} catch (GGAPIObjectAddressException e) {
			throw new GGAPIObjectQueryException(e);
		}
	}
	
	@Override
	public Object getValue(Object object, GGAPIObjectAddress fieldAddress) throws GGAPIObjectQueryException {
		if( object == null ) {
			throw new GGAPIObjectQueryException(GGAPICoreExceptionCode.UNKNOWN_ERROR, "Object is null");
		}
		List<Object> field = this.find(fieldAddress);
		return new GGAPIObjectFieldGetter(object.getClass(), field, fieldAddress).getValue(object);
	}

	@Override
	public Object setValue(String fieldAddress, Object fieldValue) throws GGAPIObjectQueryException {
		try {
			return this.setValue(new GGAPIObjectAddress(fieldAddress), fieldValue);
		} catch (GGAPIObjectAddressException e) {
			throw new GGAPIObjectQueryException(e);
		}
	}

	@Override
	public Object getValue(String fieldAddress) throws GGAPIObjectQueryException {
		try {
			return this.getValue(new GGAPIObjectAddress(fieldAddress));
		} catch (GGAPIObjectAddressException e) {
			throw new GGAPIObjectQueryException(e);
		}
	}

	@Override
	public Object setValue(GGAPIObjectAddress fieldAddress, Object fieldValue) throws GGAPIObjectQueryException {
		List<Object> field = this.find(fieldAddress);
		return new GGAPIObjectFieldSetter(this.objectClass, field, fieldAddress).setValue(this.object, fieldValue);
	}

	@Override
	public Object getValue(GGAPIObjectAddress fieldAddress) throws GGAPIObjectQueryException {
		List<Object> field = this.find(fieldAddress);
		return new GGAPIObjectFieldGetter(this.objectClass, field, fieldAddress).getValue(this.object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object fieldValueStructure(GGAPIObjectAddress address) throws GGAPIObjectQueryException {
		List<Object> fields = this.find(address);
		Object structure = null;
		
		for( int i = fields.size()-1; i >= 0; i-- ) {
			if( i == fields.size()-1 ) {
				try {
					structure = GGAPIFields.instanciate((Field) fields.get(i));
				} catch (GGAPIFieldsException e) {
					throw new GGAPIObjectQueryException(e);
				}
			} else if( GGAPIFields.isArrayOrMapOrCollectionField((Field) fields.get(i))) {
				ArrayList<Object> list = (ArrayList<Object>) GGAPIObjectReflectionHelper.newArrayListOf(((Field) fields.get(i)).getType());
				list.add(structure);
				structure = list;
			}
		}
		
		return structure;
	}

	@Override
	public Object fieldValueStructure(String address) throws GGAPIObjectQueryException {
		try {
			return this.fieldValueStructure(new GGAPIObjectAddress(address));
		} catch (GGAPIObjectAddressException e) {
			throw new GGAPIObjectQueryException(e);
		}
	}

	@Override
	public Object invoke(String methodAddress, Object... args) throws GGAPIObjectQueryException {
		try {
			return this.invoke(this.object, new GGAPIObjectAddress(methodAddress), args);
		} catch (GGAPIObjectAddressException e) {
			throw new GGAPIObjectQueryException(e);
		}
	}

	@Override
	public Object invoke(GGAPIObjectAddress methodAddress, Object... args) throws GGAPIObjectQueryException {
		return this.invoke(this.object, methodAddress, args);
	}

	@Override
	public Object invoke(Object object, GGAPIObjectAddress methodAddress, Object... args) throws GGAPIObjectQueryException {
		if( object == null ) {
			throw new GGAPIObjectQueryException(GGAPICoreExceptionCode.UNKNOWN_ERROR, "Object is null");
		}

		List<Object> field = this.find(methodAddress);
		return new GGAPIObjectMethodInvoker(object.getClass(), field, methodAddress).invoke(object, args);
	}
}
