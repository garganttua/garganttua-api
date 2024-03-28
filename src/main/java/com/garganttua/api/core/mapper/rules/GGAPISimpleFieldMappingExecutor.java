package com.garganttua.api.core.mapper.rules;

import java.lang.reflect.Field;

import com.garganttua.api.core.mapper.IGGAPIMappingRuleExecutor;
import com.garganttua.api.core.objects.utils.GGAPIFieldAccessManager;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelper;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelperExcpetion;

public class GGAPISimpleFieldMappingExecutor implements IGGAPIMappingRuleExecutor {

	private Field sourceField;
	private Field destinationField;

	public GGAPISimpleFieldMappingExecutor(Field sourceField, Field destinationField) {
		this.sourceField = sourceField;
		this.destinationField = destinationField;
	}

	@Override
	public <destination> destination doMapping(Class<destination> destinationClass, destination destinationObject, Object sourceObject) throws GGAPIMappingRuleExecutorException {
		if( destinationObject == null ) {
			try {
				destinationObject = GGAPIObjectReflectionHelper.instanciateNewObject(destinationClass);
			} catch (GGAPIObjectReflectionHelperExcpetion e) {
				throw new GGAPIMappingRuleExecutorException(e);
			}
		}

		try ( GGAPIFieldAccessManager accessor = new GGAPIFieldAccessManager(destinationField) ){
			try ( GGAPIFieldAccessManager accessor2 = new GGAPIFieldAccessManager(this.sourceField) ){
				this.destinationField.set(destinationObject, this.sourceField.get(sourceObject));
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new GGAPIMappingRuleExecutorException(e);
		}
		
		return destinationObject;
	}

}
