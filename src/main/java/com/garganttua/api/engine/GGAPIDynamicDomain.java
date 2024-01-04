package com.garganttua.api.engine;

import java.util.Arrays;

import com.garganttua.api.repository.dao.GGAPIDao;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.GGAPICrudAccess;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;

public record GGAPIDynamicDomain(
		String domain,
		Class<IGGAPIEntity> entityClass, 
		Class<IGGAPIDTOObject<IGGAPIEntity>> dtoClass, 
		GGAPIDao db,
		String ws,
		String controller,
		String business, 
		String event,
		String connector,
		String repo,
		String dao, 
		boolean allow_creation,
		boolean allow_read_all, 
		boolean allow_read_one, 
		boolean allow_update_one, 
		boolean allow_delete_one,
		boolean allow_delete_all, 
		boolean allow_count, 
		GGAPICrudAccess creation_access,
		GGAPICrudAccess read_all_access, 
		GGAPICrudAccess read_one_access, 
		GGAPICrudAccess update_one_access,
		GGAPICrudAccess delete_one_access, 
		GGAPICrudAccess delete_all_access, 
		GGAPICrudAccess count_access,
		boolean creation_authority, 
		boolean read_all_authority, 
		boolean read_one_authority,
		boolean update_one_authority, 
		boolean delete_one_authority, 
		boolean delete_all_authority,
		boolean count_authority, 
		boolean hiddenable, 
		boolean publicEntity, 
		String shared, 
		String geolocalized, 
		boolean tenantEntity,
		String[] unicity, 
		String[] mandatory, 
		boolean showTenantId, 
		boolean ownedEntity, 
		boolean ownerEntity, 
		boolean authenticatorEntity) {
	
	@Override
	public String toString() {
		return String.format(
			"[domain [%s], entityClass [%s], dtoClass [%s], db [%s], ws [%s], controller [%s], business [%s], " +
	        "event [%s], connector [%s], repo [%s], dao [%s], allow_creation [%s], allow_read_all [%s], allow_read_one [%s], " +
	        "allow_update_one [%s], allow_delete_one [%s], allow_delete_all [%s], allow_count [%s], creation_access [%s], " +
	        "read_all_access [%s], read_one_access [%s], update_one_access [%s], delete_one_access [%s], delete_all_access [%s], " +
	        "count_access [%s], creation_authority [%s], read_all_authority [%s], read_one_authority [%s], " +
	        "update_one_authority [%s], delete_one_authority [%s], delete_all_authority [%s], count_authority [%s], " +
	        "hiddenable [%s], publicEntity [%s], shared [%s], geolocalized [%s], tenantEntity [%s], unicity [%s], " +
	        "mandatory [%s], showTenantId [%s], ownedEntity [%s], ownerEntity [%s], authenticatorEntity [%s]]",
	        domain, entityClass, dtoClass, db, ws, controller, business, event, connector, repo, dao, allow_creation,
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
	
	public IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> getDomain() {
		return new IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>() {
	
			@Override
			public Class<IGGAPIEntity> getEntityClass() {
				return entityClass;
			}
	
			@Override
			public Class<IGGAPIDTOObject<IGGAPIEntity>> getDtoClass() {
				return dtoClass;
			}
	
			@Override
			public String getDomain() {
				return domain;
			}
		};
	}

}
