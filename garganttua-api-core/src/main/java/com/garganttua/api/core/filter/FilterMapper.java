package com.garganttua.api.core.filter;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.entity.tools.EntityHelper;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.dto.DtoInfos;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.objects.mapper.GGMapper;
import com.garganttua.objects.mapper.GGMapperException;
import com.garganttua.objects.mapper.GGMappingConfiguration;
import com.garganttua.objects.mapper.rules.GGMappingRule;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilterMapper implements IFilterMapper {
	
	private GGMapper mapper = DefaultMapper.mapper();

	@Override
	public List<Pair<Class<?>, IFilter>> map(IDomain domain, IFilter filter) throws CoreException {
		if( log.isDebugEnabled() ) {
			log.debug("Mapping Filter {} for domain {}", filter, domain);
		}
		
		List<Pair<Class<?>, IFilter>> filters = new ArrayList<Pair<Class<?>,IFilter>>();

		for( Pair<Class<?>, DtoInfos> destinationClass: domain.getDtos() ) {
			if( filter == null ) {
				filters.add(new Pair<Class<?>, IFilter>(destinationClass.getValue0(), null));
				continue;
			}

			List<GGMappingRule> mappingRules = null;
			try {
				GGMappingConfiguration mappingConfiguration = this.mapper.getMappingConfiguration(domain.getEntityClass(), destinationClass.getValue0());
				mappingRules = mappingConfiguration.destinationRules();
			} catch (GGMapperException e) {
				throw new EngineException(e);
			}
			if( log.isDebugEnabled() ) {
				log.debug("Creating new filter from filter {} with rules {}", filter, mappingRules);
			}

			IFilter mappedFilter = this.map(mappingRules, filter, null);
			if( mappedFilter != null ) {
				filters.add(new Pair<Class<?>, IFilter>(destinationClass.getValue0(), mappedFilter));
			}
		}
		
		Object entityExample = EntityHelper.newExampleInstance(domain.getEntityClass(), filter);

		for(Pair<Class<?>, IFilter> p: filters) {
			Object dtoExample;
			try {
				dtoExample = this.mapper.map(entityExample, p.getValue0()); 
			} catch (GGMapperException e) {
				throw new EngineException(e);
			}
			this.setCorrespondingValuesToFilter(p.getValue1(), dtoExample);
		}

		return filters;
	}

	private void setCorrespondingValuesToFilter(IFilter filter, Object dtoExample) throws CoreException {
		if( filter == null ) {	
			return;
		}
		if( filter.getName().equals(Literal.OPERATOR_FIELD) ) {
			String fieldAddress = (String) filter.getValue();
			Object value = null;
			try {
				value = GGObjectQueryFactory.objectQuery(dtoExample.getClass()).getValue(dtoExample, fieldAddress);
			} catch (GGReflectionException e) {
				throw new EngineException(e);
			}
			if( value != null ) 
				filter.getLiterals().get(0).setValue(value);
		} else {
			for( IFilter sub: filter.getLiterals() ) {
				this.setCorrespondingValuesToFilter(sub, dtoExample);
			}
		}
	}

	private IFilter map(List<GGMappingRule> mappingRules, IFilter filter, IFilter parent) throws CoreException {
		if( filter == null ) {
			return null;
		}
		IFilter filterCloned = filter.clone();
		if( filterCloned.getName().equals(Literal.OPERATOR_FIELD) ) {
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
					throw new EngineException(e);
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
			for( IFilter literal: filterCloned.clone().getLiterals()) {
				IFilter mappedFilter = this.map(mappingRules, literal.clone(), filterCloned);
				if( mappedFilter != null ) {
					filterCloned.replaceSubLiteral(literal, mappedFilter);
				}
			}
		}
		return filterCloned;
	}
}
