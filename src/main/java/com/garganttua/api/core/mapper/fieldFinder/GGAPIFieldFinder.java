package com.garganttua.api.core.mapper.fieldFinder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.core.mapper.IGGAPIFieldFinder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIFieldFinder implements IGGAPIFieldFinder {
	
	@Override
    public Pair<Field, Class<?>> findField(Class<?> objectClass, String fieldAddress) throws GGAPIFieldFinderException {
		if( log.isDebugEnabled() ) {
			log.debug("Looking for field "+fieldAddress+" in "+objectClass);
		}
		String[] parts = fieldAddress.split("\\.");
        
        return findFieldRecursively(objectClass, parts, 0);
    }
    
    private Pair<Field, Class<?>> findFieldRecursively(Class<?> clazz, String[] parts, int index) throws GGAPIFieldFinderException {
    	if( log.isDebugEnabled() ) {
			log.debug("Looking for field "+parts[index]+" in "+clazz);
		}
    	
    	if (clazz == null || index >= parts.length) {
        	throw new GGAPIFieldFinderException("Field "+parts[index]+" not found in class "+clazz);
        }
        try {
			Field field = clazz.getDeclaredField(parts[index]);
		
			if( field == null ) {
				if( clazz.getSuperclass() != null ) {
					return this.findFieldRecursively(clazz.getSuperclass(), parts, index);
				}
			} else {
				if( index == parts.length -1 ) {
					return new Pair<Field, Class<?>>(field, clazz);
				} else {
					Class<?> fieldType = field.getType();
			        if ( Collection.class.isAssignableFrom(fieldType) ) {
			        	Class<?> genericType = GGAPIFieldFinder.getGenericType(field, 0);
			        	return this.findFieldRecursively(genericType, parts, index+1);
			        } else if ( Map.class.isAssignableFrom(fieldType) ) {
			        	
			        	if( parts[index+1].equals("value") ) {
			        		Class<?> genericType = GGAPIFieldFinder.getGenericType(field, 1);
				        	return this.findFieldRecursively(genericType, parts, index+2);
			        	} else if( parts[index+1].equals("key") ) {
			        		Class<?> genericType = GGAPIFieldFinder.getGenericType(field, 0);
				        	return this.findFieldRecursively(genericType, parts, index+2);
			        	} else {
			        		throw new GGAPIFieldFinderException("Field "+parts[index]+" is a map, so address must indicate key or value");
			        	}
			        	
			        } else {
			        	return this.findFieldRecursively(field.getType(), parts, index+1);
			        }
				}
			}

        } catch (NoSuchFieldException | SecurityException e) {
			if( clazz.getSuperclass() != null ) {
				return this.findFieldRecursively(clazz.getSuperclass(), parts, index);
			}
		}
        throw new GGAPIFieldFinderException("Field "+parts[index]+" not found in class "+clazz);
    }
    
    
   static public Class<?> getGenericType(Field field, int genericTypeIndex) {
        Type fieldType = field.getGenericType();
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0 && typeArguments[genericTypeIndex] instanceof Class<?>) {
                return (Class<?>) typeArguments[genericTypeIndex];
            }
        }
        return null;
    }
}
