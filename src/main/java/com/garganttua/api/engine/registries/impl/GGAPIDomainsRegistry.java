package com.garganttua.api.engine.registries.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.engine.GGAPIDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.registries.IGGAPIDomainsRegistry;
import com.garganttua.api.repository.dao.GGAPIDao;
import com.garganttua.api.security.authorization.GGAPITokenProviderType;
import com.garganttua.api.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.security.keys.GGAPIKeyManagerType;
import com.garganttua.api.security.keys.managers.mongo.GGAPIKeyRealmEntity;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service(value = "dynamicDomainsRegistry")
public class GGAPIDomainsRegistry implements IGGAPIDomainsRegistry {
	
	@Value("${com.garganttua.api.engine.packages}")
	protected String[] scanPackages;
	
	@Value("${com.garganttua.api.security.key.manager.type:inmemory}")
	protected GGAPIKeyManagerType keyManagerType;
	
	@Value("${com.garganttua.api.security.authorization.tokens.provider}")
	protected GGAPITokenProviderType tokenProviderType;
	
	@Value("${com.garganttua.api.security:disabled}")
	protected String securityEnabled;
	
	@Getter
	private List<GGAPIDomain> domains;

	private boolean onwerFound = false;
	
	@PostConstruct
	public void init() throws GGAPIEngineException {
		log.info("Creating Dynamic Domains ...");
		this.domains = new ArrayList<GGAPIDomain>();
		boolean tenantFound = false;

		for (String pack : this.scanPackages) {
			log.info("	Scanning package " + pack);

			Reflections reflections = new Reflections(pack);

			Set<Class<?>> entities__ = reflections.getTypesAnnotatedWith(GGAPIEntity.class, false);

			for (Class<?> clazz : entities__) {
				
				GGAPIDomain domain;
				try {
					domain = GGAPIDomain.fromEntityClass(clazz, this.scanPackages);
					
					if( domain == null ) {
						log.warn("Found entity "+clazz.getName()+" with annotation @GGAPIEntity, but unable to retrieve the corespounding dynamic domain. Ignoring");
						continue;
					}
							
					if (domain.entity.getValue1().tenantEntity() && !tenantFound) {
						tenantFound = true;
					} else if (domain.entity.getValue1().tenantEntity() && !tenantFound) {
						throw new GGAPIEngineException("There are more than one entity declared as tenantEntity.");
					}
					
					if( this.securityEnabled.equals("enabled") && domain.entity.getValue1().domain().equals(GGAPIKeyRealmEntity.domain) && this.keyManagerType == GGAPIKeyManagerType.mongo ) {
//						domain.dto.db = GGAPIDao.MONGO;
					}
					
					if( this.securityEnabled.equals("enabled") && domain.entity.getValue1().domain().equals(GGAPIToken.domain) && this.tokenProviderType == GGAPITokenProviderType.mongo ) {
//						dynamicDomain.db = GGAPIDao.MONGO;
					}
					
					if( !this.onwerFound && domain.entity.getValue1().ownerEntity()) {
						this.onwerFound = true;
					} else if( this.onwerFound && domain.entity.getValue1().ownerEntity() ) {
						throw new GGAPIEngineException("More than one owner entity found");
					}
					
					if( !this.securityEnabled.equals("enabled") && domain.entity.getValue1().domain().equals(GGAPIKeyRealmEntity.domain) ) {
						continue;
					}
					if( !this.securityEnabled.equals("enabled") && domain.entity.getValue1().domain().equals(GGAPIToken.domain) ) {
						continue;
					}
					if( domain.entity.getValue1().domain().equals(GGAPIKeyRealmEntity.domain) && this.keyManagerType != GGAPIKeyManagerType.mongo ) {
						continue;
					} 
					if( domain.entity.getValue1().equals(GGAPIToken.domain) && this.tokenProviderType != GGAPITokenProviderType.mongo ) {
						continue;
					}
					
					if( domain.dtos.size() == 0 ) {
						log.error("No class annotated with @GGAPIDto found for entity "+clazz.getName());
						throw new GGAPIDtoException("No class annotated with @GGAPIDto found for entity "+clazz.getName());
					}
					
					this.domains.add(domain);
					
					log.info("	Dynamic Domain Added "+domain.toString());
				} catch (GGAPIEntityException | GGAPIDtoException e) {
					throw new GGAPIEngineException(e);
				}
			}
		}
	}

	@Override
	public GGAPIDomain getDomain(String domain) {
		for( GGAPIDomain ddomain: this.domains ) {
			if( ddomain.entity.getValue1().domain().toLowerCase().equals(domain)) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public GGAPIDomain getOwnerDomain() {
		for( GGAPIDomain ddomain: this.domains ) {
			if( ddomain.entity.getValue1().ownerEntity() == true) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public GGAPIDomain getTenantDomain() {
		for( GGAPIDomain ddomain: this.domains ) {
			if( ddomain.entity.getValue1().tenantEntity() == true) {
				return ddomain;
			}
		}
		return null;
	}
}
