package com.garganttua.api.engine;

import java.util.Arrays;

import com.garganttua.api.core.GGAPICrudAccess;
import com.garganttua.api.core.GGAPIEntity;
import com.garganttua.api.core.GGAPIEntityHelper;
import com.garganttua.api.core.GGAPIObjectsHelper;
import com.garganttua.api.core.GGAPIOwner;
import com.garganttua.api.core.GGAPITenant;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.IGGAPIEntityWithTenant;
import com.garganttua.api.core.IGGAPIHiddenableEntity;
import com.garganttua.api.core.IGGAPIOwnedEntity;
import com.garganttua.api.core.IGGAPIOwner;
import com.garganttua.api.core.IGGAPITenant;
import com.garganttua.api.repository.dao.GGAPIDao;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.repository.dto.IGGAPIHiddenableDTO;
import com.garganttua.api.security.authentication.GGAPIAuthenticator;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GGAPIDynamicDomain {
		public String domain;
		public Class<? extends IGGAPIEntity> entityClass;
		public Class<? extends IGGAPIDTOObject<? extends IGGAPIEntity>> dtoClass;
		public GGAPIDao db;
		public String ws;
		public String event;
		public String repo;
		public String dao;
		public boolean allow_creation;
		public boolean allow_read_all; 
		public boolean allow_read_one;
		public boolean allow_update_one; 
		public boolean allow_delete_one;
		public boolean allow_delete_all; 
		public boolean allow_count;
		public GGAPICrudAccess creation_access;
		public GGAPICrudAccess read_all_access; 
		public GGAPICrudAccess read_one_access;
		public GGAPICrudAccess update_one_access;
		public GGAPICrudAccess delete_one_access;
		public GGAPICrudAccess delete_all_access;
		public GGAPICrudAccess count_access;
		public boolean creation_authority;
		public boolean read_all_authority;
		public boolean read_one_authority;
		public boolean update_one_authority;
		public boolean delete_one_authority; 
		public boolean delete_all_authority;
		public boolean count_authority;
		public boolean hiddenable;
		public boolean publicEntity;
		public String shared;
		public String geolocalized;
		public boolean tenantEntity;
		public String[] unicity;
		public String[] mandatory;
		public boolean showTenantId;
		public boolean ownedEntity;
		public boolean ownerEntity;
		public boolean authenticatorEntity;
	
	@Override
	public String toString() {
		return String.format(
			"[domain [%s], entityClass [%s], dtoClass [%s], db [%s], ws [%s], " +
	        "event [%s], repo [%s], dao [%s], allow_creation [%s], allow_read_all [%s], allow_read_one [%s], " +
	        "allow_update_one [%s], allow_delete_one [%s], allow_delete_all [%s], allow_count [%s], creation_access [%s], " +
	        "read_all_access [%s], read_one_access [%s], update_one_access [%s], delete_one_access [%s], delete_all_access [%s], " +
	        "count_access [%s], creation_authority [%s], read_all_authority [%s], read_one_authority [%s], " +
	        "update_one_authority [%s], delete_one_authority [%s], delete_all_authority [%s], count_authority [%s], " +
	        "hiddenable [%s], publicEntity [%s], shared [%s], geolocalized [%s], tenantEntity [%s], unicity [%s], " +
	        "mandatory [%s], showTenantId [%s], ownedEntity [%s], ownerEntity [%s], authenticatorEntity [%s]]",
	        domain, entityClass, dtoClass, db, ws, event, repo, dao, allow_creation,
	        allow_read_all, allow_read_one, allow_update_one, allow_delete_one, allow_delete_all, allow_count,
	        creation_access, read_all_access, read_one_access, update_one_access, delete_one_access, delete_all_access,
	        count_access, creation_authority, read_all_authority, read_one_authority, update_one_authority,
	        delete_one_authority, delete_all_authority, count_authority, hiddenable, publicEntity, shared, geolocalized,
	        tenantEntity, arrayToString(unicity), arrayToString(mandatory), showTenantId, ownedEntity, ownerEntity,
	        authenticatorEntity
	    );
	}

	private String arrayToString(String[] array) {
	    return array != null ? Arrays.toString(array) : "null";
	}
		
	 @SuppressWarnings("unchecked")
	 static public GGAPIDynamicDomain fromEntityClass(Class<?> clazz) throws GGAPIEngineException {
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
		
		try {
			dtoClass.getConstructor(String.class, entityClass);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new GGAPIEngineException("The DTO Class must delaclare a constructor with parameters (String tenantId, Entity entity)", e);
		}
		
		if (clazz.isAnnotationPresent(GGAPITenant.class) ) {
			GGAPIObjectsHelper.isImplementingInterface(clazz, IGGAPITenant.class);
			tenant = true;
		}

		if (clazz.isAnnotationPresent(GGAPIOwner.class) ) {
			GGAPIObjectsHelper.isImplementingInterface(clazz, IGGAPIOwner.class);
			owner = true;
		}
		
		if (clazz.isAnnotationPresent(GGAPIAuthenticator.class) ) {
			authenticator = true;
			GGAPIObjectsHelper.isImplementingInterface(clazz, IGGAPIEntityWithTenant.class);
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
		String event = entityAnnotation.eventPublisher();
		String repo = entityAnnotation.repository();
		String dao = entityAnnotation.dao();

		return new GGAPIDynamicDomain(domain, entityClass, dtoClass, db, ws,
				event, repo, dao, allow_creation, allow_read_all, allow_read_one,
				allow_update_one, allow_delete_one, allow_delete_all, allow_count, creation_access,
				read_all_access, read_one_access, update_one_access, delete_one_access, delete_all_access,
				count_access, creation_authority, read_all_authority, read_one_authority,
				update_one_authority, delete_one_authority, delete_all_authority, count_authority,
				hiddenable, publicEntity, shared, geolocalized, tenant, unicity, mandatory, showTenantId, ownedEntity, owner, authenticator);
	}

}
