package com.garganttua.api.core.filter;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.mapper.GGAPIDefaultMapper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.dto.GGAPIDtoInfos;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.objects.mapper.GGMapper;
import com.garganttua.objects.mapper.GGMapperException;
import com.garganttua.objects.mapper.GGMappingConfiguration;
import com.garganttua.objects.mapper.rules.GGMappingRule;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIFilterMapper implements IGGAPIFilterMapper {
	
	private GGMapper mapper = GGAPIDefaultMapper.mapper();

	@Override
	public List<Pair<Class<?>, IGGAPIFilter>> map(IGGAPIDomain domain, IGGAPIFilter filter) throws GGAPIException {
		if( log.isDebugEnabled() ) {
			log.debug("Mapping Filter {} for domain {}", filter, domain);
		}
		
		List<Pair<Class<?>, IGGAPIFilter>> filters = new ArrayList<Pair<Class<?>,IGGAPIFilter>>();

		for( Pair<Class<?>, GGAPIDtoInfos> destinationClass: domain.getDtos() ) {
			if( filter == null ) {
				filters.add(new Pair<Class<?>, IGGAPIFilter>(destinationClass.getValue0(), null));
				continue;
			}

			List<GGMappingRule> mappingRules = null;
			try {
				GGMappingConfiguration mappingConfiguration = this.mapper.getMappingConfiguration(domain.getEntity().getValue0(), destinationClass.getValue0());
				mappingRules = mappingConfiguration.destinationRules();
			} catch (GGMapperException e) {
				throw new GGAPIEngineException(e);
			}
			if( log.isDebugEnabled() ) {
				log.debug("Creating new filter from filter {} with rules {}", filter, mappingRules);
			}

			IGGAPIFilter mappedFilter = this.map(mappingRules, filter, null);
			if( mappedFilter != null ) {
				filters.add(new Pair<Class<?>, IGGAPIFilter>(destinationClass.getValue0(), mappedFilter));
			}
		}
		
		Object entityExample = GGAPIEntityHelper.newExampleInstance(domain.getEntity().getValue0(), filter);

		for(Pair<Class<?>, IGGAPIFilter> p: filters) {
			Object dtoExample;
			try {
				dtoExample = this.mapper.map(entityExample, p.getValue0()); 
			} catch (GGMapperException e) {
				throw new GGAPIEngineException(e);
			}
			this.setCorrespondingValuesToFilter(p.getValue1(), dtoExample);
		}

		return filters;
	}

	private void setCorrespondingValuesToFilter(IGGAPIFilter filter, Object dtoExample) throws GGAPIException {
		if( filter == null ) {	
			return;
		}
		if( filter.getName().equals(GGAPILiteral.OPERATOR_FIELD) ) {
			String fieldAddress = (String) filter.getValue();
			Object value = null;
			try {
				value = GGObjectQueryFactory.objectQuery(dtoExample.getClass()).getValue(dtoExample, fieldAddress);
			} catch (GGReflectionException e) {
				throw new GGAPIEngineException(e);
			}
			filter.getLiterals().get(0).setValue(value);
		} else {
			for( IGGAPIFilter sub: filter.getLiterals() ) {
				this.setCorrespondingValuesToFilter(sub, dtoExample);
			}
		}
	}

	private IGGAPIFilter map(List<GGMappingRule> mappingRules, IGGAPIFilter filter, IGGAPIFilter parent) throws GGAPIException {
		if( filter == null ) {
			return null;
		}
		IGGAPIFilter filterCloned = filter.clone();
		if( filterCloned.getName().equals(GGAPILiteral.OPERATOR_FIELD) ) {
			String fieldAddress = (String) filterCloned.getValue();
			if( log.isDebugEnabled() ) {
				log.debug("Looking for coresponding mapping rule for field with address {}", fieldAddress);
			}
			boolean found = false;
			for( GGMappingRule rule: mappingRules ) {
				try {
					if( rule.sourceFieldAddress().equals(new GGObjectAddress(fieldAddress)) ) {
						filterCloned.setValue(rule.destinationFieldAddress().toString());
						found = true;
						break;
					}
				} catch (GGReflectionException e) {
					throw new GGAPIEngineException(e);
				}
			}
			if( !found && parent != null ) {
				parent.removeSubLiteral(filterCloned);
				return null;
			} else if( !found ) {
				return null;
			}
		} else {
			if( log.isDebugEnabled() ) {
				log.debug("Parsing sub literals of {}", filterCloned.getName());
			}
			for( IGGAPIFilter literal: filterCloned.clone().getLiterals()) {
				IGGAPIFilter mappedFilter = this.map(mappingRules, literal.clone(), filterCloned);
				if( mappedFilter != null ) {
					filterCloned.replaceSubLiteral(literal, mappedFilter);
				}
			}
		}
		return filterCloned;
	}
}
