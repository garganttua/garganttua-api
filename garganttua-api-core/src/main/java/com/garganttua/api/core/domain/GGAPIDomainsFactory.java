package com.garganttua.api.core.domain;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIDomainsFactory {
	
	private List<String> packages;
	private boolean tenantFound = false;
	private boolean onwerFound = false;
	private Set<IGGAPIDomain>  domains;

	public GGAPIDomainsFactory(List<String> packages) {
		this.packages = packages;
	}

	public Collection<IGGAPIDomain> getDomains() {
		if( this.domains != null ) {
			return this.domains;
		}
		
		this.domains = new HashSet<IGGAPIDomain>();
		
		this.packages.parallelStream().forEach(package_ -> {
			try {
				List<Class<?>> annotatedClasses = GGObjectReflectionHelper.getClassesWithAnnotation(package_, GGAPIEntity.class);
				annotatedClasses.forEach( annotatedClass -> {
					try {
						domains.add(processAnnotatedEntity(annotatedClass));
					} catch (GGAPIException e) {
						e.printStackTrace();
					}
				});
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		});	
		return domains;
	}

	private GGAPIDomain processAnnotatedEntity(Class<?> annotatedClass) throws GGAPIException {
		GGAPIDomain domain = GGAPIDomain.fromEntityClass(annotatedClass, this.packages);
			
		if( domain == null ) {
			log.warn("Found entity "+annotatedClass.getName()+" with annotation @GGAPIEntity, but unable to retrieve the corespounding dynamic domain. Ignoring");
			return null;
		}
				
		if (domain.getEntity().getValue1().tenantEntity() && !tenantFound) {
			tenantFound = true;
		} else if (domain.getEntity().getValue1().tenantEntity() && !tenantFound) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "There are more than one entity declared as tenantEntity.");
		}
		
//			if( this.securityEnabled.equals("enabled") && domain.entity.getValue1().domain().equals(GGAPIKeyRealmEntity.domain) && this.keyManagerType == GGAPIKeyManagerType.mongo ) {
////				domain.dto.db = GGAPIDao.MONGO;
//			}
//			
//			if( this.securityEnabled.equals("enabled") && domain.entity.getValue1().domain().equals(GGAPIToken.domain) && this.tokenProviderType == GGAPITokenProviderType.mongo ) {
////				dynamicDomain.db = GGAPIDao.MONGO;
//			}
		
		if( !this.onwerFound && domain.getEntity().getValue1().ownerEntity()) {
			this.onwerFound = true;
		} else if( this.onwerFound && domain.getEntity().getValue1().ownerEntity() ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "More than one owner entity found");
		}
		
//			if( !this.securityEnabled.equals("enabled") && domain.entity.getValue1().domain().equals(GGAPIKeyRealmEntity.domain) ) {
//				continue;
//			}
//			if( !this.securityEnabled.equals("enabled") && domain.entity.getValue1().domain().equals(GGAPIToken.domain) ) {
//				continue;
//			}
//			if( domain.entity.getValue1().domain().equals(GGAPIKeyRealmEntity.domain) && this.keyManagerType != GGAPIKeyManagerType.mongo ) {
//				continue;
//			} 
//			if( domain.entity.getValue1().equals(GGAPIToken.domain) && this.tokenProviderType != GGAPITokenProviderType.mongo ) {
//				continue;
//			}
		
		if( domain.getDtos().size() == 0 ) {
			log.error("No class annotated with @GGAPIDto found for entity "+annotatedClass.getName());
			throw new GGAPIDtoException(GGAPIExceptionCode.NO_DTO_FOUND, "No class annotated with @GGAPIDto found for entity "+annotatedClass.getName());
		}
		
		log.info("	Dynamic Domain Added "+domain.toString());
		return domain;
	}

	public IGGAPIDomainsRegistry getRegistry() {
		if( this.domains == null ) {
			this.getDomains();
		}
		
		return new GGAPIDomainsRegistry(this.domains);
	}

}
