package com.garganttua.api.core.mapper.rules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javatuples.Pair;

import com.garganttua.api.core.mapper.GGAPIMapperException;
import com.garganttua.api.core.mapper.GGAPIMappingDirection;
import com.garganttua.api.core.mapper.IGGAPIMappingRuleExecutor;
import com.garganttua.api.core.mapper.annotations.GGAPIFieldMappingRule;
import com.garganttua.api.core.mapper.annotations.GGAPIObjectMappingRule;
import com.garganttua.api.core.mapper.fieldFinder.GGAPIFieldFinder;
import com.garganttua.api.core.mapper.fieldFinder.GGAPIFieldFinderException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIMappingRules {

	public static List<GGAPIMappingRule> parse(Class<?> destinationClass) throws GGAPIMappingRuleException {
		List<GGAPIMappingRule> mappingRules = new ArrayList<GGAPIMappingRule>();
		return GGAPIMappingRules.recursiveParsing(destinationClass, mappingRules, "");
	}

	private static List<GGAPIMappingRule> recursiveParsing(Class<?> destinationClass, List<GGAPIMappingRule> mappingRules, String fieldAddress) throws GGAPIMappingRuleException {
		if( log.isDebugEnabled() ) {
			log.debug("Looking for mapping rules in "+destinationClass+ " address "+fieldAddress);
		}
		boolean objectMapping = false;
		if( destinationClass.isAnnotationPresent(GGAPIObjectMappingRule.class) ) {
			GGAPIObjectMappingRule annotation = destinationClass.getDeclaredAnnotation(GGAPIObjectMappingRule.class);
			mappingRules.add(new GGAPIMappingRule(null, null, null, destinationClass, GGAPIMappingRules.getMethod(destinationClass, annotation.fromSourceMethod()), GGAPIMappingRules.getMethod(destinationClass, annotation.toSourceMethod())));
			objectMapping = true;
		}
		
		for( Field field: destinationClass.getDeclaredFields() ) {
			if( field.isAnnotationPresent(GGAPIFieldMappingRule.class) && !objectMapping){
				GGAPIFieldMappingRule annotation = field.getDeclaredAnnotation(GGAPIFieldMappingRule.class);
				
				Method fromSourceMethod = null;
				if( !annotation.fromSourceMethod().isEmpty() ) {
					GGAPIMappingRules.getMethod(destinationClass, annotation.fromSourceMethod());
				}
				Method toSourceMethod = null;
				if( !annotation.toSourceMethod().isEmpty() ) {
					GGAPIMappingRules.getMethod(destinationClass, annotation.toSourceMethod());
				}
				
				mappingRules.add(new GGAPIMappingRule(annotation.sourceFieldAddress(), fieldAddress+field.getName(), field, destinationClass, fromSourceMethod, toSourceMethod));
			} else if( GGAPIMappingRules.isNotPrimitive(field) ){
				GGAPIMappingRules.recursiveParsing(field.getType(), mappingRules, fieldAddress+field.getName()+".");
			} else if ( Collection.class.isAssignableFrom(field.getType()) ) {
				GGAPIMappingRules.recursiveParsing(GGAPIFieldFinder.getGenericType(field, 0), mappingRules, fieldAddress+field.getName()+".");
	        } else if ( Map.class.isAssignableFrom(field.getType()) ) {
	        	GGAPIMappingRules.recursiveParsing(GGAPIFieldFinder.getGenericType(field, 0), mappingRules, fieldAddress+field.getName()+".key.");
	        	GGAPIMappingRules.recursiveParsing(GGAPIFieldFinder.getGenericType(field, 1), mappingRules, fieldAddress+field.getName()+".value.");	        	
	        }
		}
		if( destinationClass.getSuperclass() != null ) {
			GGAPIMappingRules.recursiveParsing(destinationClass.getSuperclass(), mappingRules, fieldAddress);
		}
		return mappingRules;
	}

	private static Method getMethod(Class<?> clazz, String methodName) throws GGAPIMappingRuleException {
		for( Method method: clazz.getDeclaredMethods() ) {
			if( methodName.equals(method.getName()) ) {
				return method;
			}
		}
		throw new GGAPIMappingRuleException("The method "+methodName+" not found in object "+clazz);
	}
	
    private static boolean isNotPrimitive(Field field) {
        if (field.getType().isPrimitive()) {
            return false; 
        }

        if (field.getType() == Integer.class ||
            field.getType() == Long.class ||
            field.getType() == Float.class ||
            field.getType() == Double.class ||
            field.getType() == Short.class ||
            field.getType() == Byte.class ||
            field.getType() == Character.class ||
            field.getType() == Boolean.class) {
            return false; 
        }

        if (Map.class.isAssignableFrom(field.getType()) ||
            List.class.isAssignableFrom(field.getType()) ||
            Set.class.isAssignableFrom(field.getType()) ||
            Collection.class.isAssignableFrom(field.getType())) {
            return false;
        }

        return true;
    }

	public static void validate(GGAPIMappingDirection mappingDirection, Class<?> destinationClass, Class<?> class1, List<GGAPIMappingRule> rules) throws GGAPIMappingRuleException {
		
	}

	public static IGGAPIMappingRuleExecutor getRuleExecutor(GGAPIMappingDirection mappingDirection, GGAPIMappingRule rule, Object source, Class<?> destinationClass) throws GGAPIMapperException {
		Pair<Field, Class<?>> destinationField = null;
		Pair<Field, Class<?>> sourceField = null;
		
		try {
			if( mappingDirection == GGAPIMappingDirection.REGULAR ) {
				sourceField = new GGAPIFieldFinder().findField(source.getClass(), rule.sourceFieldAddress());
				destinationField = new GGAPIFieldFinder().findField(destinationClass, rule.destinationFieldAddress());
			} else {
				sourceField = new GGAPIFieldFinder().findField(source.getClass(), rule.destinationFieldAddress());
				destinationField = new GGAPIFieldFinder().findField(destinationClass, rule.sourceFieldAddress());
			}
			
			if( sourceField.getValue0().getType().equals(destinationField.getValue0().getType()) ) {
				return new GGAPISimpleFieldMappingExecutor(sourceField.getValue0(), destinationField.getValue0());
			}

		} catch (GGAPIFieldFinderException e) {
			throw new GGAPIMapperException(e.getMessage(), e);
		}

		return null;
	}

	
}
