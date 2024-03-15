package com.garganttua.api.core.mapper;

import java.util.List;

import com.garganttua.api.core.mapper.rules.GGAPIMappingRule;
import com.garganttua.api.core.mapper.rules.GGAPIMappingRuleException;
import com.garganttua.api.core.mapper.rules.GGAPIMappingRuleExecutorException;
import com.garganttua.api.core.mapper.rules.GGAPIMappingRules;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIMapper implements IGGAPIMapper {
	
	@Override
	public <destination> destination map(Object source, Class<destination> destinationClass) throws GGAPIMapperException {
		if( log.isDebugEnabled() ) {
			log.debug("Mapping {} to {}", source, destinationClass);
		}
		try {
			List<GGAPIMappingRule> destinationRules = GGAPIMappingRules.parse(destinationClass);
			List<GGAPIMappingRule> sourceRules = GGAPIMappingRules.parse(source.getClass());
			
			GGAPIMappingDirection mappingDirection = this.determineMapingDirection(sourceRules, destinationRules);
			
			switch( mappingDirection ) {
			case REGULAR:
				return this.doMapping(mappingDirection, destinationClass, source, destinationRules);
			case REVERSE:
				return this.doMapping(mappingDirection, destinationClass, source, sourceRules);
			}
			
		} catch (GGAPIMappingRuleException e) {
			throw new GGAPIMapperException(GGAPIMapperException.OBJECT_MAPPING, e);
		}
		return null;
	}
	
	private <destination> destination doMapping(GGAPIMappingDirection mappingDirection, Class<destination> destinationClass, Object source, List<GGAPIMappingRule> rules) throws GGAPIMapperException {
		try {
			GGAPIMappingRules.validate(mappingDirection, destinationClass, source.getClass(), rules);
		} catch (GGAPIMappingRuleException e) {
			throw new GGAPIMapperException(GGAPIMapperException.OBJECT_MAPPING, e);
		}
		
		destination destObject = null;
		
		for( GGAPIMappingRule rule: rules ) {		
			IGGAPIMappingRuleExecutor executor = GGAPIMappingRules.getRuleExecutor(mappingDirection, rule, source, destinationClass);		
			try {
				destObject = executor.doMapping(destinationClass, destObject, source);
			} catch (GGAPIMappingRuleExecutorException  e) {
				throw new GGAPIMapperException(GGAPIMapperException.OBJECT_MAPPING, e);
			}
		}
		return destObject;
	}

	private GGAPIMappingDirection determineMapingDirection(List<GGAPIMappingRule> sourceRules, List<GGAPIMappingRule> destinationRules) throws GGAPIMapperException {
		if( sourceRules.size() ==0 && destinationRules.size() != 0 ) {
			return GGAPIMappingDirection.REGULAR;
		} else if( sourceRules.size() !=0 && destinationRules.size() == 0 ){
			return GGAPIMappingDirection.REVERSE;
		} else {
			throw new GGAPIMapperException(GGAPIMapperException.OBJECT_MAPPING, "Cannot determine mapping direction as source and destination are annotated with mapping rules");
		}
	}

}
