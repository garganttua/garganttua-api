package com.garganttua.api.core.mapper.rules;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.core.mapper.GGAPIMapperException;
import com.garganttua.api.core.mapper.GGAPIMappingDirection;
import com.garganttua.api.core.mapper.IGGAPIMappingRuleExecutor;
import com.garganttua.api.core.mapper.annotations.GGAPIFieldMappingRule;
import com.garganttua.api.core.mapper.annotations.GGAPIObjectMappingRule;
import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.fields.GGAPIFields;
import com.garganttua.api.core.objects.query.GGAPIObjectQuery;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;

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
			GGAPIObjectAddress fromSourceMethod = new GGAPIObjectAddress(annotation.fromSourceMethod());
			GGAPIObjectAddress toSourceMethod = new GGAPIObjectAddress(annotation.toSourceMethod());
			mappingRules.add(new GGAPIMappingRule(null, null, destinationClass, fromSourceMethod, toSourceMethod));
			objectMapping = true;
		}
		
		for( Field field: destinationClass.getDeclaredFields() ) {
			if( field.isAnnotationPresent(GGAPIFieldMappingRule.class) && !objectMapping){
				GGAPIFieldMappingRule annotation = field.getDeclaredAnnotation(GGAPIFieldMappingRule.class);
				
				GGAPIObjectAddress fromSourceMethod = null;
				if( !annotation.fromSourceMethod().isEmpty() ) {
					fromSourceMethod = new GGAPIObjectAddress(annotation.fromSourceMethod());
				}
				GGAPIObjectAddress toSourceMethod = null;
				if( !annotation.toSourceMethod().isEmpty() ) {
					toSourceMethod = new GGAPIObjectAddress(annotation.toSourceMethod());
				}
				
				GGAPIObjectAddress sourceFieldAddress = new GGAPIObjectAddress(annotation.sourceFieldAddress());
				GGAPIObjectAddress destFieldAddress = new GGAPIObjectAddress(fieldAddress+field.getName());
				
				mappingRules.add(new GGAPIMappingRule(sourceFieldAddress, destFieldAddress, destinationClass, fromSourceMethod, toSourceMethod));
			} else {
				if( GGAPIFields.isNotPrimitive(field.getType()) && 
						!Collection.class.isAssignableFrom(field.getType()) &&
						!Map.class.isAssignableFrom(field.getType()) &&
						field.getType().isArray()){
					GGAPIMappingRules.recursiveParsing(field.getType(), mappingRules, fieldAddress+field.getName()+GGAPIObjectAddress.ELEMENT_SEPARATOR);
				} else if ( Collection.class.isAssignableFrom(field.getType()) ) {
					GGAPIMappingRules.recursiveParsing(GGAPIFields.getGenericType(field, 0), mappingRules, fieldAddress+field.getName()+GGAPIObjectAddress.ELEMENT_SEPARATOR);
		        } else if ( Map.class.isAssignableFrom(field.getType()) ) {
		        	GGAPIMappingRules.recursiveParsing(GGAPIFields.getGenericType(field, 0), mappingRules, fieldAddress+field.getName()+GGAPIObjectAddress.ELEMENT_SEPARATOR+GGAPIObjectAddress.MAP_KEY_INDICATOR+GGAPIObjectAddress.ELEMENT_SEPARATOR);
		        	GGAPIMappingRules.recursiveParsing(GGAPIFields.getGenericType(field, 1), mappingRules, fieldAddress+field.getName()+GGAPIObjectAddress.ELEMENT_SEPARATOR+GGAPIObjectAddress.MAP_VALUE_INDICATOR+GGAPIObjectAddress.ELEMENT_SEPARATOR);	        	
		        }
			}
		}
		if( destinationClass.getSuperclass() != null ) {
			GGAPIMappingRules.recursiveParsing(destinationClass.getSuperclass(), mappingRules, fieldAddress);
		}
		return mappingRules;
	}

	
	public static void validate(GGAPIMappingDirection mappingDirection, Class<?> destinationClass, Class<?> class1, List<GGAPIMappingRule> rules) throws GGAPIMappingRuleException {
		
	}

	public static IGGAPIMappingRuleExecutor getRuleExecutor(GGAPIMappingDirection mappingDirection, GGAPIMappingRule rule, Object source, Class<?> destinationClass) throws GGAPIMapperException {
		List<Object> destinationField = null;
		List<Object> sourceField = null;
		
		try {
			if( mappingDirection == GGAPIMappingDirection.REGULAR ) {
				sourceField = new GGAPIObjectQuery(source.getClass()).find(rule.sourceFieldAddress());
				destinationField = new GGAPIObjectQuery(destinationClass).find(rule.destinationFieldAddress());
			} else {
				sourceField = new GGAPIObjectQuery(source.getClass()).find(rule.destinationFieldAddress());
				destinationField = new GGAPIObjectQuery(destinationClass).find(rule.sourceFieldAddress());
			}
			
//			if( ((Field) sourceField.get(sourceField.size()-1).getValue0()).getType().equals(((Field) destinationField.get(destinationField.size()-1).getValue0()).getType()) ) {
//				return new GGAPISimpleFieldMappingExecutor(((Field) sourceField.get(sourceField.size()-1).getValue0()), ((Field) destinationField.get(destinationField.size()-1).getValue0()));
//			}

		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIMapperException(e.getMessage(), e);
		}

		return null;
	}

	
}
