package com.garganttua.api.core.entity.checker;

import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityGeolocalized;

import lombok.NoArgsConstructor;

@GGAPIEntity(
		domain = "test", 
		interfaces = { "" }		
)
@GGAPIEntityGeolocalized(location = "test")
@NoArgsConstructor
class GeolocEntity extends GenericGGAPIEntity {
	protected GeolocEntity(String uuid, String id) {
		super(uuid, id);
	}

	private String test;
}



//@GGAPIEntity (
//		domain = "tenants"
//	)
//	class TestGenericEntity extends GenericGGAPIEntity {
//
//	}
//
//@GGAPIEntity (
//		domain = "tenants",
//		creation_access = GGAPIServiceAccess.anonymous,
//		count_access = GGAPIServiceAccess.tenant,
//		delete_one_access = GGAPIServiceAccess.tenant,
//		read_one_access = GGAPIServiceAccess.tenant,
//		update_one_access = GGAPIServiceAccess.tenant,
//		read_all_access = GGAPIServiceAccess.tenant,
//		delete_all_access = GGAPIServiceAccess.tenant,
//		allow_count = true,
//		allow_creation = true,
//		allow_delete_all = true,
//		allow_delete_one = true,
//		allow_read_all = true,
//		allow_read_one = true,
//		allow_update_one = true,
//		count_authority = true,
//		creation_authority = false,
//		delete_one_authority = true,
//		read_all_authority = true,
//		read_one_authority = true,
//		update_one_authority = true,
//		delete_all_authority = true
//	)
//	@Getter
//	@GGAPIAuthenticator
//	@GGAPIEntityTenant
//	@GGAPIEntityOwner
//	@GGAPIEntityHiddenable
//	@GGAPIEntityGeolocalized
//	@GGAPIEntityPublic 
//	@GGAPIEntityShared
//	class TestValidationResult  extends GenericGGAPIEntity {
////		public TestValidationResult() {
////			
////		}
//		@GGAPIEntityUuid
//		@GGAPIEntityOwnerId
//		@GGAPIEntityTenantId
//		protected String uuid;
//		
//		@GGAPIEntityId
//		@GGAPIEntityUnicity
//		@GGAPIEntityMandatory
//		protected String id;
//		
//		@GGAPIAuthenticatorLogin
//		@JsonProperty
//		protected String email;
//		
//		@JsonInclude
//		private String name;
//		
//		@JsonInclude
//		@GGAPIEntityShare
//		private String surname;
//		
//		@JsonInclude
//		@GGAPIAuthenticatorPassword
//		@GGAPIEntityMandatory
//		private String password;
//		
//		@JsonInclude
//		@Setter
//		@GGAPIAuthenticatorAuthorities
//		private List<String> userAuthorities;
//		
//		@JsonIgnore
//		@GGAPIAuthenticatorAccountNonExpired
//		@GGAPIAuthenticatorAccountNonLocked
//		@GGAPIAuthenticatorCredentialsNonExpired
//		@GGAPIAuthenticatorEnabled
//		@GGAPIEntityHidden
//		private boolean enabled = true;
//		
//		@GGAPIEntitySuperTenant
//		@GGAPIEntityGotFromRepository
//		private boolean superTenant;
//		
//		@GGAPIEntitySuperOwner
//		private boolean superOwner;
//		
//		@GGAPIEntityLocation
//		@GGAPIEntityUnicity
//		private Point location;
//
//		@Inject
//		@JsonIgnore
//		private IGGAPIAccessRulesRegistry accessRulesRegistry;
//
//	}
//
//
//
//
//@GGAPIEntity (
//		domain = "tenants",
//		creation_access = GGAPIServiceAccess.anonymous,
//		count_access = GGAPIServiceAccess.tenant,
//		delete_one_access = GGAPIServiceAccess.tenant,
//		read_one_access = GGAPIServiceAccess.tenant,
//		update_one_access = GGAPIServiceAccess.tenant,
//		read_all_access = GGAPIServiceAccess.tenant,
//		delete_all_access = GGAPIServiceAccess.tenant,
//		allow_count = true,
//		allow_creation = true,
//		allow_delete_all = true,
//		allow_delete_one = true,
//		allow_read_all = true,
//		allow_read_one = true,
//		allow_update_one = true,
//		count_authority = true,
//		creation_authority = false,
//		delete_one_authority = true,
//		read_all_authority = true,
//		read_one_authority = true,
//		update_one_authority = true,
//		delete_all_authority = true
//	)
//	@Getter
//	@GGAPIAuthenticator
//	@GGAPIEntityTenant
//	@GGAPIEntityOwned
//	@GGAPIEntityHiddenable
//	@GGAPIEntityGeolocalized
//	@GGAPIEntityPublic 
//	@GGAPIEntityShared
//	class TestValidationResult2 extends GenericGGAPIEntity {
////		public TestValidationResult2() {
////			
////		}
//
//		@GGAPIEntityUuid
//		@GGAPIEntityOwnerId
//		@GGAPIEntityTenantId
//		protected String uuid;
//		
//		@GGAPIEntityId
//		@GGAPIEntityUnicity
//		@GGAPIEntityMandatory
//		protected String id;
//		
//		@GGAPIAuthenticatorLogin
//		@JsonProperty
//		protected String email;
//		
//		@JsonInclude
//		private String name;
//		
//		@JsonInclude
//		@GGAPIEntityShare
//		private String surname;
//		
//		@JsonInclude
//		@GGAPIAuthenticatorPassword
//		@GGAPIEntityMandatory
//		private String password;
//		
//		@JsonInclude
//		@Setter
//		@GGAPIAuthenticatorAuthorities
//		private List<String> userAuthorities;
//		
//		@JsonIgnore
//		@GGAPIAuthenticatorAccountNonExpired
//		@GGAPIAuthenticatorAccountNonLocked
//		@GGAPIAuthenticatorCredentialsNonExpired
//		@GGAPIAuthenticatorEnabled
//		@GGAPIEntityHidden
//		private boolean enabled = true;
//		
//		@GGAPIEntitySuperTenant
//		@GGAPIEntityGotFromRepository
//		private boolean superTenant;
//		
//		@GGAPIEntitySuperOwner
//		private boolean superOwner;
//		
//		@GGAPIEntityLocation
//		@GGAPIEntityUnicity
//		private Point location;
//
//		@Inject
//		@JsonIgnore
//		private IGGAPIAccessRulesRegistry accessRulesRegistry;
//
//	}
//
//@GGAPIEntity (
//		domain = "tenants", 
//		creation_access = GGAPIServiceAccess.anonymous,
//		count_access = GGAPIServiceAccess.tenant,
//		delete_one_access = GGAPIServiceAccess.tenant,
//		read_one_access = GGAPIServiceAccess.tenant,
//		update_one_access = GGAPIServiceAccess.tenant,
//		read_all_access = GGAPIServiceAccess.tenant,
//		delete_all_access = GGAPIServiceAccess.tenant,
//		allow_count = true,
//		allow_creation = true,
//		allow_delete_all = true,
//		allow_delete_one = true,
//		allow_read_all = true,
//		allow_read_one = true,
//		allow_update_one = true,
//		count_authority = true,
//		creation_authority = false,
//		delete_one_authority = true,
//		read_all_authority = true,
//		read_one_authority = true,
//		update_one_authority = true,
//		delete_all_authority = true
//	)
//	@NoArgsConstructor
//	@Getter
//	@GGAPIAuthenticator
//	@GGAPIEntityTenant
//	@GGAPIEntityOwned
//	@GGAPIEntityHiddenable
//	@GGAPIEntityGeolocalized
//	@GGAPIEntityPublic 
//	@GGAPIEntityShared
//	class TestBusinessMethodsPresence  extends GenericGGAPIEntity {
//
//		@GGAPIEntityUuid
//		@GGAPIEntityOwnerId
//		@GGAPIEntityTenantId
//		protected String uuid;
//		
//		@GGAPIEntityId
//		@GGAPIEntityUnicity
//		@GGAPIEntityMandatory
//		protected String id;
//		
//		@GGAPIAuthenticatorLogin
//		@JsonProperty
//		protected String email;
//		
//		@JsonInclude
//		private String name;
//		
//		@JsonInclude
//		@GGAPIEntityShare
//		private String surname;
//		
//		@JsonInclude
//		@GGAPIAuthenticatorPassword
//		@GGAPIEntityMandatory
//		private String password;
//		
//		@JsonInclude
//		@Setter
//		@GGAPIAuthenticatorAuthorities
//		private List<String> userAuthorities;
//		
//		@JsonIgnore
//		@GGAPIAuthenticatorAccountNonExpired
//		@GGAPIAuthenticatorAccountNonLocked
//		@GGAPIAuthenticatorCredentialsNonExpired
//		@GGAPIAuthenticatorEnabled
//		@GGAPIEntityHidden
//		private boolean enabled = true;
//		
//		@GGAPIEntitySuperTenant
//		@GGAPIEntityGotFromRepository
//		private boolean superTenant;
//		
//		@GGAPIEntitySuperOwner
//		private boolean superOwner;
//		
//		@GGAPIEntityLocation
//		@GGAPIEntityUnicity
//		private Point location;
//
//		@Inject
//		@JsonIgnore
//		private IGGAPIAccessRulesRegistry accessRulesRegistry;
//		
//		@GGAPIEntityAfterGet
//		private void afterGet(IGGAPICaller caller, Map<String, String> params) {
//		}
//		
//		@GGAPIEntityBeforeCreate
//		private void beforeCreate(IGGAPICaller caller, Map<String, String> params) {
//		}
//		
//		@GGAPIEntityAfterCreate
//		private void afterCreate(IGGAPICaller caller, Map<String, String> params) {
//		}
//		
//		@GGAPIEntityBeforeUpdate
//		private void beforeUpdate(IGGAPICaller caller, Map<String, String> params) {
//		}
//		
//		@GGAPIEntityAfterUpdate
//		private void afterUpdate(IGGAPICaller caller, Map<String, String> params) {
//		}
//		
//		@GGAPIEntityBeforeDelete
//		private void beforeDelete(IGGAPICaller caller, Map<String, String> params) {
//		}
//		
//		@GGAPIEntityAfterDelete
//		private void afterDelete(IGGAPICaller caller, Map<String, String> params) {
//		}
//
//	}
//
//@GGAPIEntity (
//		domain = "tenants", 
//		creation_access = GGAPIServiceAccess.anonymous,
//		count_access = GGAPIServiceAccess.tenant,
//		delete_one_access = GGAPIServiceAccess.tenant,
//		read_one_access = GGAPIServiceAccess.tenant,
//		update_one_access = GGAPIServiceAccess.tenant,
//		read_all_access = GGAPIServiceAccess.tenant,
//		delete_all_access = GGAPIServiceAccess.tenant,
//		allow_count = true,
//		allow_creation = true,
//		allow_delete_all = true,
//		allow_delete_one = true,
//		allow_read_all = true,
//		allow_read_one = true,
//		allow_update_one = true,
//		count_authority = true,
//		creation_authority = false,
//		delete_one_authority = true,
//		read_all_authority = true,
//		read_one_authority = true,
//		update_one_authority = true,
//		delete_all_authority = true
//	)
//	@Getter
//	@GGAPIAuthenticator
//	@GGAPIEntityTenant
//	@GGAPIEntityOwned
//	@GGAPIEntityHiddenable
//	@GGAPIEntityGeolocalized
//	@GGAPIEntityPublic 
//	@GGAPIEntityShared
//	class TestAuthorizeUpdate extends GenericGGAPIEntity {
//		
//		public TestAuthorizeUpdate() {
//			super();
//			this.uuid = "hdusoidhqs";
//		}
//		
//		public TestAuthorizeUpdate(String toto) {
//			super();
//			this.uuid = "hdusoidhqs";
//		}
//
//		@GGAPIEntityUuid
//		@GGAPIEntityOwnerId
//		@GGAPIEntityTenantId
//		protected String uuid;
//		
//		@GGAPIEntityId
//		@GGAPIEntityUnicity
//		@GGAPIEntityMandatory
//		protected String id;
//		
//		@GGAPIAuthenticatorLogin
//		@JsonProperty
//		protected String email;
//		
//		@JsonInclude
//		private String name;
//		
//		@JsonInclude
//		@GGAPIEntityShare
//		private String surname;
//		
//		@JsonInclude
//		@GGAPIAuthenticatorPassword
//		@GGAPIEntityMandatory
//		private String password;
//		
//		@JsonInclude
//		@Setter
//		@GGAPIAuthenticatorAuthorities
//		private List<String> userAuthorities;
//		
//		@JsonIgnore
//		@GGAPIAuthenticatorAccountNonExpired
//		@GGAPIAuthenticatorAccountNonLocked
//		@GGAPIAuthenticatorCredentialsNonExpired
//		@GGAPIAuthenticatorEnabled
//		@GGAPIEntityHidden
//		private boolean enabled = true;
//		
//		@GGAPIEntitySuperTenant
//		@GGAPIEntityGotFromRepository
//		private boolean superTenant;
//		
//		@GGAPIEntitySuperOwner
//		@GGAPIEntityAuthorizeUpdate(authority = "test")
//		private boolean superOwner;
//		
//		@GGAPIEntityLocation
//		@GGAPIEntityUnicity
//		private Point location;
//
//		@Inject
//		@JsonIgnore
//		private IGGAPIAccessRulesRegistry accessRulesRegistry;
//	
//	}
//
//@GGAPIEntity(
//		domain = "entity"
//)
//class TestAnnotationEntityUuid extends GenericGGAPIEntity {
//	public TestAnnotationEntityUuid() {
//		
//	}
//	@GGAPIEntityUuid
//	private String uuid;
//	@GGAPIEntityId
//	private String id;
//	@GGAPIEntityGotFromRepository
//	private boolean t;
//	
//}
//
//@GGAPIEntity(
//		domain = "entity"
//)
//@GGAPIEntityOwner(ownerId = "uuid", superOwner = "superOwner")
//class TestOwnerEntityWithFieldValue  extends GenericGGAPIEntity {
////	public TestOwnerEntityWithFieldValue() {
////		
////	}
//	
//	private String uuid;
//	
//	private boolean superOwner;
//	
//	@GGAPIEntityUuid
//	private String tuuid;
//	
//	@GGAPIEntityId
//	private String id;
//	@GGAPIEntityGotFromRepository
//	private boolean t;
//}
//
//@GGAPIEntity(
//		domain = "entity"
//)
//class TestAnnotationEntityUuidFromSuperClass extends GenericGGAPIEntity {
////	public TestAnnotationEntityUuidFromSuperClass() {
////		
////	}
//	private String uuid;
//	@GGAPIEntityGotFromRepository
//	private boolean t;
//	
//}
//
//@GGAPIEntity(
//		domain = "entity"
//)
//@GGAPIEntityGeolocalized(location = "location")
//class TestAnnotationGeolocalized extends GenericGGAPIEntity {
////	public TestAnnotationGeolocalized() {
////		
////	}
//	@GGAPIEntityGotFromRepository
//	private boolean t;
//	Point location;
//}
//
//@GGAPIEntity(
//		domain = "entity"
//)
//@GGAPIEntityOwner
//@GGAPIEntityOwned
//class TestAnnotationOwnedAndOwner extends GenericGGAPIEntity{
//	
//}
//
//@GGAPIEntity(
//		domain = "entity"
//)
//class TestAnnotationEntityIdFromSuperClass extends GenericGGAPIEntity {
////	public TestAnnotationEntityIdFromSuperClass() {
////		
////	}
//	private String uuid;
//}
//
//@GGAPIEntity(
//		domain = "entity"
//)
//@GGAPIEntityOwned(ownerId = "uuid")
//class TestOwnedEntityWithFieldValue  extends GenericGGAPIEntity {
////	public TestOwnedEntityWithFieldValue() {
////		
////	}
//	private String uuid;
//}
//
//@GGAPIEntity(
//		domain = "entity"
//)
//class TestAnnotationEntityId extends GenericGGAPIEntity {
////	public TestAnnotationEntityId() {
////		
////	}
//}
//
//@GGAPIEntity(
//		domain = "entity"
//)
//@GGAPIEntityTenant(tenantId = "uuid", superTenant = "superTenant")
//class TestTenantEntityWithFieldValue  extends GenericGGAPIEntity{
////	public TestTenantEntityWithFieldValue() {
////		
////	}
//	private String uuid;
//	
//	private boolean superTenant;
//	@GGAPIEntityUuid
//	private String tuuid;
//	@GGAPIEntityId
//	private String id;
//}
//
//@GGAPIEntity(
//		domain = "entity"
//)
//@GGAPIEntityOwner(superOwner = "superOwner")
//class TestOwnerEntityWithAnnotatedField  extends GenericGGAPIEntity{
////	public TestOwnerEntityWithAnnotatedField() {
////		
////	}
//	@GGAPIEntityOwnerId
//	private String uuid;
//	
//	private boolean superOwner;
//	
//	@GGAPIEntityUuid
//	private String tuuid;
//	@GGAPIEntityId
//	private String id;
//	
//}
//
//@GGAPIEntity(
//		domain = "entity"
//)
//@GGAPIEntityTenant(superTenant = "superTenant")
//class TestTenantEntityWithAnnotatedField  extends GenericGGAPIEntity{
////	public TestTenantEntityWithAnnotatedField() {
////		
////	}
//	@GGAPIEntityTenantId
//	private String uuid;
//	
//	private boolean superTenant;
//	@GGAPIEntityUuid
//	private String tuuid;
//	@GGAPIEntityId
//	private String id;
//	
//}
//
//@GGAPIEntity(
//		domain = "entity"
//)
//@GGAPIEntityShared(share = "uuid")
//class TestSharedEntityWithFieldValue  extends GenericGGAPIEntity {
//	
//	private String uuid;
//}
