package com.garganttua.api.core.filter.mapper;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.dto.checker.GGAPIDtoChecker.GGAPIDtoInfos;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.mapper.GGAPIMapper;
import com.garganttua.api.core.mapper.GGAPIMapperException;
import com.garganttua.api.core.mapper.rules.GGAPIMappingRule;
import com.garganttua.api.core.mapper.rules.GGAPIMappingRuleException;
import com.garganttua.api.core.mapper.rules.GGAPIMappingRules;
import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.query.GGAPIObjectQuery;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.engine.GGAPIDomain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIFilterMapper implements IGGAPIFilterMapper {
	
	private GGAPIMapper mapper = new GGAPIMapper();

	@Override
	public List<Pair<Class<?>, GGAPILiteral>> map(GGAPIDomain domain, GGAPILiteral filter) throws GGAPILiteralMapperException {
		if( log.isDebugEnabled() ) {
			log.debug("Mapping Filter {} for domain {}", filter, domain);
		}
		
		List<Pair<Class<?>, GGAPILiteral>> filters = new ArrayList<Pair<Class<?>,GGAPILiteral>>();
		
		for( Pair<Class<?>, GGAPIDtoInfos> destinationClass: domain.dtos ) {
			try {
				List<GGAPIMappingRule> mappingRules = GGAPIMappingRules.parse(destinationClass.getValue0());
				if( log.isDebugEnabled() ) {
					log.debug("Creating new filter from filter {} with rules {}", filter, mappingRules);
				}
				GGAPILiteral mappedFilter = this.map(mappingRules, filter.clone(), null);
				filters.add(new Pair<Class<?>, GGAPILiteral>(destinationClass.getValue0(), mappedFilter));
			} catch (GGAPIMappingRuleException e) {
				throw new GGAPILiteralMapperException(e);
			}
		}
		
		try {
			Object entityExample = GGAPIEntityHelper.newExampleInstance(domain.entity.getValue0(), filter);

			for(Pair<Class<?>, GGAPILiteral> p: filters) {
				Object dtoExample = this.mapper.map(entityExample, p.getValue0());
				this.setCorrespondingValuesToFilter(p.getValue1(), dtoExample);
			}
			
		} catch (GGAPIEntityException | GGAPIMapperException e) {
			throw new GGAPILiteralMapperException(e);
		}

		return filters;
	}

	private void setCorrespondingValuesToFilter(GGAPILiteral filter, Object dtoExample) throws GGAPILiteralMapperException {
		if( filter.getName().equals(GGAPILiteral.OPERATOR_FIELD) ) {
			String fieldAddress = (String) filter.getValue();
			Object value = null;
			try {
				value = new GGAPIObjectQuery(dtoExample.getClass()).getValue(dtoExample, fieldAddress);
			} catch (GGAPIObjectQueryException e) {
				throw new GGAPILiteralMapperException(e);
			}
			filter.getLiterals().get(0).setValue(value);
		} else {
			for( GGAPILiteral sub: filter.getLiterals() ) {
				setCorrespondingValuesToFilter(sub, dtoExample);
			}
		}
	}

	private GGAPILiteral map(List<GGAPIMappingRule> mappingRules, GGAPILiteral filter, GGAPILiteral parent) {
		
		if( filter.getName().equals(GGAPILiteral.OPERATOR_FIELD) ) {
			String fieldAddress = (String) filter.getValue();
			if( log.isDebugEnabled() ) {
				log.debug("Looking for coresponding mapping rule for field with address {}", fieldAddress);
			}
			boolean found = false;
			for( GGAPIMappingRule rule: mappingRules ) {
				if( rule.sourceFieldAddress().equals(new GGAPIObjectAddress(fieldAddress)) ) {
					filter.setValue(rule.destinationFieldAddress().toString());
					found = true;
					break;
				}
			}
			if( !found && parent != null ) {
				parent.removeSubLiteral(filter);
				return null;
			} else if( !found ) {
				return null;
			}
		} else {
			if( log.isDebugEnabled() ) {
				log.debug("Parsing sub literals of {}", filter.getName());
			}
			for( GGAPILiteral literal: filter.clone().getLiterals()) {
				GGAPILiteral mappedFilter = this.map(mappingRules, literal.clone(), filter);
				if( mappedFilter != null ) {
					filter.replaceSubLiteral(literal, mappedFilter);
				}
			}
		}
		return filter;
	}
}
