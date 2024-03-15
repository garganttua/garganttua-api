package com.garganttua.api.core.filter.mapper;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.dto.checker.GGAPIDtoChecker.GGAPIDtoInfos;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.mapper.rules.GGAPIMappingRule;
import com.garganttua.api.core.mapper.rules.GGAPIMappingRuleException;
import com.garganttua.api.core.mapper.rules.GGAPIMappingRules;
import com.garganttua.api.engine.GGAPIDomain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIFilterMapper implements IGGAPIFilterMapper{

	@Override
	public List<Pair<Class<?>, GGAPILiteral>> map(GGAPIDomain domain, GGAPILiteral filter) throws GGAPILiteralMapperException {
		if( log.isDebugEnabled() ) {
			log.debug("Mapping Filter {} for domain {}", filter, domain);
		}
		
		List<Pair<Class<?>, GGAPILiteral>> filters = new ArrayList<Pair<Class<?>,GGAPILiteral>>();
		
		for( Pair<Class<?>, GGAPIDtoInfos> destinationClass: domain.dtos ) {
			try {
				List<GGAPIMappingRule> mappingRules = GGAPIMappingRules.parse(destinationClass.getValue0());
				filters.add(new Pair<Class<?>, GGAPILiteral>(destinationClass.getValue0(), this.map(mappingRules, filter)));
			} catch (GGAPIMappingRuleException e) {
				throw new GGAPILiteralMapperException(e);
			}
		}
		
		return filters;
	}

	private GGAPILiteral map(List<GGAPIMappingRule> mappingRules, GGAPILiteral filter) {
		if( log.isDebugEnabled() ) {
			log.debug("Creating new filter from filter {} with rules {}", filter, mappingRules);
		}
		return filter;
	}

}
