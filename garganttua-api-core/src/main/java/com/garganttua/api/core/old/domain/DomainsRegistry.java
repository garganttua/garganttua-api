package com.garganttua.api.core.domain;

import java.util.Set;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.domain.IDomainsRegistry;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.objects.mapper.GGMapperException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DomainsRegistry implements IDomainsRegistry {

	@Getter
	private Set<IDomain> domains;
	
	@Setter
	private IEngine engine;

	public DomainsRegistry(Set<IDomain> domains) {
		this.domains = domains;
		
		domains.stream().forEach(domain -> {
			Class<?> entityClass = domain.getEntityClass();
			domain.getDtos().stream().forEach(dto -> {
				Class<?> dtoClass = dto.getValue0();
				try {
					DefaultMapper.mapper().recordMappingConfiguration(entityClass, dtoClass);
					DefaultMapper.mapper().recordMappingConfiguration(dtoClass, entityClass);
				} catch (GGMapperException e) {
					log.atWarn().log("Error", e);
				}
			});
		});
	}

	@Override
	public IDomain getDomain(String domain) {
		for( IDomain ddomain: this.domains ) {
			if( ddomain.getDomain().toLowerCase().equals(domain)) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public IDomain getOwnerDomain() {
		for( IDomain ddomain: this.domains ) {
			if( ddomain.isOwnerEntity() == true) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public IDomain getTenantDomain() {
		for( IDomain ddomain: this.domains ) {
			if( ddomain.isTenantEntity() == true) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public void setDomain(IDomain domain) {
	}

}
