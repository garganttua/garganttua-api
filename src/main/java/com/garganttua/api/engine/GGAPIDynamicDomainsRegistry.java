package com.garganttua.api.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.garganttua.api.repository.dao.GGAPIDao;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.repository.dto.IGGAPIHiddenableDTO;
import com.garganttua.api.security.authentication.GGAPIAuthenticator;
import com.garganttua.api.security.authentication.IGGAPIAuthenticator;
import com.garganttua.api.spec.GGAPICrudAccess;
import com.garganttua.api.spec.GGAPIEntity;
import com.garganttua.api.spec.GGAPIEntityHelper;
import com.garganttua.api.spec.GGAPIObjectsHelper;
import com.garganttua.api.spec.GGAPIOwner;
import com.garganttua.api.spec.GGAPITenant;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.IGGAPIEntityWithTenant;
import com.garganttua.api.spec.IGGAPIHiddenableEntity;
import com.garganttua.api.spec.IGGAPIOwnedEntity;
import com.garganttua.api.spec.IGGAPIOwner;
import com.garganttua.api.spec.IGGAPITenant;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service(value = "dynamicDomainsRegistry")
public class GGAPIDynamicDomainsRegistry implements IGGAPIDynamicDomainsRegistry {
	
	@Value("${com.garganttua.api.engine.packages}")
	protected String[] scanPackages;

	@Getter
	private List<GGAPIDynamicDomain> dynamicDomains;
	
	@Bean 
	public List<IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>> domains(){
		List<IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>> domains = new ArrayList<IGGAPIDomain<IGGAPIEntity,IGGAPIDTOObject<IGGAPIEntity>>>();
		
		this.dynamicDomains.forEach( ddomain -> {
			domains.add(new IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>() {
				
				@Override
				public Class<IGGAPIEntity> getEntityClass() {
					return ddomain.entityClass();
				}
				
				@Override
				public Class<IGGAPIDTOObject<IGGAPIEntity>> getDtoClass() {
					return ddomain.dtoClass();
				}
				
				@Override
				public String getDomain() {
					return ddomain.domain();
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
				
				GGAPIDynamicDomain dynamicDomain = this.getDynamicDomainFromEntityClass(clazz);
				
				if( dynamicDomain == null ) {
					continue;
				}
				
				if (dynamicDomain.tenantEntity() && !tenantFound) {
					tenantFound = true;
				} else if (dynamicDomain.tenantEntity() && !tenantFound) {
					throw new GGAPIEngineException("There are more than one entity declared as tenantEntity.");
				}

				this.dynamicDomains.add(dynamicDomain);
				
				log.info("	Dynamic Domain Added "+dynamicDomain.toString());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private GGAPIDynamicDomain getDynamicDomainFromEntityClass(Class<?> clazz) throws GGAPIEngineException {
		Class<IGGAPIDTOObject<IGGAPIEntity>> dtoClass = null;
		GGAPIObjectsHelper.isImplementingInterface(clazz, IGGAPIEntity.class);
		
		Class<IGGAPIEntity> entityClass = (Class<IGGAPIEntity>) clazz;
		
		GGAPIEntity entityAnnotation = clazz.getAnnotation(GGAPIEntity.class);
		String domain = GGAPIEntityHelper.getDomain(entityClass);
		
		if( entityAnnotation == null ) {
			return null;
		}

		try {
			dtoClass = (Class<IGGAPIDTOObject<IGGAPIEntity>>) Class.forName(entityAnnotation.dto());
		} catch (ClassNotFoundException e) {
			throw new GGAPIEngineException(e);
		}

		boolean allow_creation = entityAnnotation.allow_creation();
		boolean allow_read_all = entityAnnotation.allow_read_all();
		boolean allow_read_one = entityAnnotation.allow_read_one();
		boolean allow_update_one = entityAnnotation.allow_update_one();
		boolean allow_delete_one = entityAnnotation.allow_delete_one();
		boolean allow_delete_all = entityAnnotation.allow_delete_all();
		boolean allow_count = entityAnnotation.allow_count();

		GGAPICrudAccess creation_access = entityAnnotation.creation_access();
		GGAPICrudAccess read_all_access = entityAnnotation.read_all_access();
		GGAPICrudAccess read_one_access = entityAnnotation.read_one_access();
		GGAPICrudAccess update_one_access = entityAnnotation.update_one_access();
		GGAPICrudAccess delete_one_access = entityAnnotation.delete_one_access();
		GGAPICrudAccess delete_all_access = entityAnnotation.delete_all_access();
		GGAPICrudAccess count_access = entityAnnotation.count_access();

		boolean creation_authority = entityAnnotation.creation_authority();
		boolean read_all_authority = entityAnnotation.read_all_authority();
		boolean read_one_authority = entityAnnotation.read_one_authority();
		boolean update_one_authority = entityAnnotation.update_one_authority();
		boolean delete_one_authority = entityAnnotation.delete_one_authority();
		boolean delete_all_authority = entityAnnotation.delete_all_authority();
		boolean count_authority = entityAnnotation.count_authority();

		boolean hiddenable = entityAnnotation.hiddenAble();
		boolean publicEntity = entityAnnotation.publicEntity();
		String shared = entityAnnotation.shared();
		String geolocalized = entityAnnotation.geolocialized();

		boolean tenant = false;
		String[] unicity = entityAnnotation.unicity();
		String[] mandatory = entityAnnotation.mandatory();
		boolean ownedEntity = entityAnnotation.ownedEntity();
		
		boolean showTenantId = entityAnnotation.showTenantId();
		
		boolean owner = false;
		boolean authenticator = false;
		
		GGAPIDao db = entityAnnotation.db();

		GGAPIObjectsHelper.isImplementingInterface(dtoClass, IGGAPIDTOObject.class);
		
		if (clazz.isAnnotationPresent(GGAPITenant.class) ) {
			GGAPIObjectsHelper.isImplementingInterface(clazz, IGGAPITenant.class);
			tenant = true;
		}

		if (clazz.isAnnotationPresent(GGAPIOwner.class) ) {
			GGAPIObjectsHelper.isImplementingInterface(clazz, IGGAPIOwner.class);
			owner = true;
		}
		
		if (clazz.isAnnotationPresent(GGAPIAuthenticator.class) ) {
			GGAPIObjectsHelper.isImplementingInterface(clazz, IGGAPIAuthenticator.class);
			authenticator = true;
		}
		
		if (hiddenable) {
			GGAPIObjectsHelper.isImplementingInterface(clazz, IGGAPIHiddenableEntity.class);
			GGAPIObjectsHelper.isImplementingInterface(dtoClass, IGGAPIHiddenableDTO.class);
		}
		
		if(showTenantId) {
			GGAPIObjectsHelper.isImplementingInterface(clazz, IGGAPIEntityWithTenant.class);
		}

		if( ownedEntity ) {
			GGAPIObjectsHelper.isImplementingInterface(clazz, IGGAPIOwnedEntity.class);
		}

		String ws = entityAnnotation.ws();
		String controller = entityAnnotation.controller();
		String event = entityAnnotation.eventPublisher();
		String business = entityAnnotation.business();
		String connector = entityAnnotation.connector();
		String repo = entityAnnotation.repository();
		String dao = entityAnnotation.dao();

		return new GGAPIDynamicDomain(domain, entityClass, dtoClass, db, ws, controller,
				business, event, connector, repo, dao, allow_creation, allow_read_all, allow_read_one,
				allow_update_one, allow_delete_one, allow_delete_all, allow_count, creation_access,
				read_all_access, read_one_access, update_one_access, delete_one_access, delete_all_access,
				count_access, creation_authority, read_all_authority, read_one_authority,
				update_one_authority, delete_one_authority, delete_all_authority, count_authority,
				hiddenable, publicEntity, shared, geolocalized, tenant, unicity, mandatory, showTenantId, ownedEntity, owner, authenticator);
	}

}
