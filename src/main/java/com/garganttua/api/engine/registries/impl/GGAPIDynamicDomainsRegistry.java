package com.garganttua.api.engine.registries.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.IGGAPIDomain;
import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.registries.IGGAPIDynamicDomainsRegistry;
import com.garganttua.api.repository.dao.GGAPIDao;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.GGAPITokenProviderType;
import com.garganttua.api.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.security.keys.GGAPIKeyManagerType;
import com.garganttua.api.security.keys.managers.mongo.GGAPIKeyRealmEntity;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service(value = "dynamicDomainsRegistry")
public class GGAPIDynamicDomainsRegistry implements IGGAPIDynamicDomainsRegistry {
	
	@Value("${com.garganttua.api.engine.packages}")
	protected String[] scanPackages;
	
	@Value("${com.garganttua.api.security.key.manager.type:inmemory}")
	private GGAPIKeyManagerType keyManagerType;
	
	@Value("${com.garganttua.api.security.authorization.tokens.provider}")
	private GGAPITokenProviderType tokenProviderType;
	
	@Value("${com.garganttua.api.security:disabled}")
	private String securityEnabled;
	
	@Getter
	private List<GGAPIDynamicDomain> dynamicDomains;

	private boolean onwerFound = false;
	
	@Bean 
	public List<IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>> domains(){
		List<IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>> domains = new ArrayList<IGGAPIDomain<IGGAPIEntity,IGGAPIDTOObject<IGGAPIEntity>>>();
		
		this.dynamicDomains.forEach( ddomain -> {
			domains.add(new IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>() {
				
				@SuppressWarnings("unchecked")
				@Override
				public Class<IGGAPIEntity> getEntityClass() {
					return (Class<IGGAPIEntity>) ddomain.entityClass;
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public Class<IGGAPIDTOObject<IGGAPIEntity>> getDtoClass() {
					return (Class<IGGAPIDTOObject<IGGAPIEntity>>) ddomain.dtoClass;
				}
				
				@Override
				public String getDomain() {
					return ddomain.domain;
				}
			});
		});
		
		return domains;
	}

	@PostConstruct
	public void init() throws GGAPIEngineException {

		log.info("============================================");
		log.info("======                                ======");
		log.info("====== Starting Garganttua API Engine ======");
		log.info("======                                ======");
		log.info("============================================");
		log.info("Version: {}", this.getClass().getPackage().getImplementationVersion());
		log.info("== BOOTING ENGINE ==");
		log.info("Creating Dynamic Domains ...");

		this.dynamicDomains = new ArrayList<GGAPIDynamicDomain>();
		boolean tenantFound = false;

		for (String pack : this.scanPackages) {
			log.info("	Scanning package " + pack);

			Reflections reflections = new Reflections(pack);

			Set<Class<?>> entities__ = reflections.getTypesAnnotatedWith(GGAPIEntity.class, false);

			for (Class<?> clazz : entities__) {
				
				GGAPIDynamicDomain dynamicDomain = GGAPIDynamicDomain.fromEntityClass(clazz);
				
				if( dynamicDomain == null ) {
					log.warn("Found entity "+clazz.getName()+" with annotation @GGAPIEntity, but unable to retrieve the corespounding dynamic domain. Ignoring");
					continue;
				}

				if (dynamicDomain.tenantEntity && !tenantFound) {
					tenantFound = true;
				} else if (dynamicDomain.tenantEntity && !tenantFound) {
					throw new GGAPIEngineException("There are more than one entity declared as tenantEntity.");
				}
				
				if( dynamicDomain.domain.equals(GGAPIKeyRealmEntity.domain) && this.keyManagerType == GGAPIKeyManagerType.mongo ) {
					dynamicDomain.db = GGAPIDao.mongo;
				}
				
				if( dynamicDomain.domain.equals(GGAPIToken.domain) && this.tokenProviderType == GGAPITokenProviderType.mongo ) {
					dynamicDomain.db = GGAPIDao.mongo;
				}
				
				if( !this.onwerFound && dynamicDomain.ownerEntity) {
					this.onwerFound = true;
				} else if( this.onwerFound && dynamicDomain.ownerEntity ) {
					throw new GGAPIEngineException("More than one owner entity found");
				}
				
				if( !this.securityEnabled.equals("enabled") && dynamicDomain.domain.equals(GGAPIKeyRealmEntity.domain) ) {
					continue;
				}
				if( !this.securityEnabled.equals("enabled") && dynamicDomain.domain.equals(GGAPIToken.domain) ) {
					continue;
				}
				if( dynamicDomain.domain.equals(GGAPIKeyRealmEntity.domain) && this.keyManagerType != GGAPIKeyManagerType.mongo ) {
					continue;
				} 
				if( dynamicDomain.domain.equals(GGAPIToken.domain) && this.tokenProviderType != GGAPITokenProviderType.mongo ) {
					continue;
				}
				
				this.dynamicDomains.add(dynamicDomain);
				
				log.info("	Dynamic Domain Added "+dynamicDomain.toString());
			}
		}
	}
	
	
	@Override
	public GGAPIDynamicDomain getDomain(HttpServletRequest request) {
		String uri = request.getRequestURI();

		String[] uriParts = uri.split("/");
		
		for (GGAPIDynamicDomain ddomain : this.dynamicDomains) {
			if (ddomain.domain.toLowerCase().equals(uriParts[1])) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public GGAPIDynamicDomain getDomain(String domain) {
		for( GGAPIDynamicDomain ddomain: this.dynamicDomains ) {
			if( ddomain.domain.toLowerCase().equals(domain)) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public GGAPIDynamicDomain getAuthenticatorDomain() {
		for( GGAPIDynamicDomain ddomain: this.dynamicDomains ) {
			if( ddomain.authenticatorEntity == true) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public GGAPIDynamicDomain getOwnerDomain() {
		for( GGAPIDynamicDomain ddomain: this.dynamicDomains ) {
			if( ddomain.ownerEntity == true) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public GGAPIDynamicDomain getTenantDomain() {
		for( GGAPIDynamicDomain ddomain: this.dynamicDomains ) {
			if( ddomain.tenantEntity == true) {
				return ddomain;
			}
		}
		return null;
	}

}
