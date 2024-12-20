package com.garganttua.api.core.domain;

import java.util.Set;

import com.garganttua.api.core.mapper.GGAPIDefaultMapper;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.domain.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.objects.mapper.GGMapperException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIDomainsRegistry implements IGGAPIDomainsRegistry {

	@Getter
	private Set<IGGAPIDomain> domains;
	
	@Setter
	private IGGAPIEngine engine;

	public GGAPIDomainsRegistry(Set<IGGAPIDomain> domains) {
		this.domains = domains;
		
		domains.stream().forEach(domain -> {
			Class<?> entityClass = domain.getEntityClass();
			domain.getDtos().stream().forEach(dto -> {
				Class<?> dtoClass = dto.getValue0();
				try {
					GGAPIDefaultMapper.mapper().recordMappingConfiguration(entityClass, dtoClass);
					GGAPIDefaultMapper.mapper().recordMappingConfiguration(dtoClass, entityClass);
				} catch (GGMapperException e) {
					log.atWarn().log("Error", e);
				}
			});
		});
	}

	@Override
	public IGGAPIDomain getDomain(String domain) {
		for( IGGAPIDomain ddomain: this.domains ) {
			if( ddomain.getDomain().toLowerCase().equals(domain)) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public IGGAPIDomain getOwnerDomain() {
		for( IGGAPIDomain ddomain: this.domains ) {
			if( ddomain.isOwnerEntity() == true) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public IGGAPIDomain getTenantDomain() {
		for( IGGAPIDomain ddomain: this.domains ) {
			if( ddomain.isTenantEntity() == true) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public void setDomain(IGGAPIDomain domain) {
	}

}
