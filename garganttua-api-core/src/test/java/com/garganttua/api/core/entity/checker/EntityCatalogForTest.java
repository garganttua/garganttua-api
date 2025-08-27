package com.garganttua.api.core.entity.checker;

import com.garganttua.api.core.entity.GenericEntity;
import com.garganttua.api.spec.entity.annotations.Entity;
import com.garganttua.api.spec.entity.annotations.EntityGeolocalized;

import lombok.NoArgsConstructor;

@Entity(
		domain = "test", 
		interfaces = { "" }		
)
@EntityGeolocalized(location = "test")
@NoArgsConstructor
class GeolocEntity extends GenericEntity {
	protected GeolocEntity(String uuid, String id) {
		super(uuid, id);
	}

	private String test;
}



//@Entity (
//		domain = "tenants"
//	)
//	class TestGenericEntity extends GenericEntity {
//
//	}
//
//@Entity (
//		domain = "tenants",
//		creation_access = ServiceAccess.anonymous,
//		count_access = ServiceAccess.tenant,
//		delete_one_access = ServiceAccess.tenant,
//		read_one_access = ServiceAccess.tenant,
//		update_one_access = ServiceAccess.tenant,
//		read_all_access = ServiceAccess.tenant,
//		delete_all_access = ServiceAccess.tenant,
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
//	@Authenticator
//	@EntityTenant
//	@EntityOwner
//	@EntityHiddenable
//	@EntityGeolocalized
//	@EntityPublic 
//	@EntityShared
//	class TestValidationResult  extends GenericEntity {
////		public TestValidationResult() {
////			
////		}
//		@EntityUuid
//		@EntityOwnerId
//		@EntityTenantId
//		protected String uuid;
//		
//		@EntityId
//		@EntityUnicity
//		@EntityMandatory
//		protected String id;
//		
//		@AuthenticatorLogin
//		@JsonProperty
//		protected String email;
//		
//		@JsonInclude
//		private String name;
//		
//		@JsonInclude
//		@EntityShare
//		private String surname;
//		
//		@JsonInclude
//		@AuthenticatorPassword
//		@EntityMandatory
//		private String password;
//		
//		@JsonInclude
//		@Setter
//		@AuthenticatorAuthorities
//		private List<String> userAuthorities;
//		
//		@JsonIgnore
//		@AuthenticatorAccountNonExpired
//		@AuthenticatorAccountNonLocked
//		@AuthenticatorCredentialsNonExpired
//		@AuthenticatorEnabled
//		@EntityHidden
//		private boolean enabled = true;
//		
//		@EntitySuperTenant
//		@EntityGotFromRepository
//		private boolean superTenant;
//		
//		@EntitySuperOwner
//		private boolean superOwner;
//		
//		@EntityLocation
//		@EntityUnicity
//		private Point location;
//
//		@Inject
//		@JsonIgnore
//		private IAccessRulesRegistry accessRulesRegistry;
//
//	}
//
//
//
//
//@Entity (
//		domain = "tenants",
//		creation_access = ServiceAccess.anonymous,
//		count_access = ServiceAccess.tenant,
//		delete_one_access = ServiceAccess.tenant,
//		read_one_access = ServiceAccess.tenant,
//		update_one_access = ServiceAccess.tenant,
//		read_all_access = ServiceAccess.tenant,
//		delete_all_access = ServiceAccess.tenant,
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
//	@Authenticator
//	@EntityTenant
//	@EntityOwned
//	@EntityHiddenable
//	@EntityGeolocalized
//	@EntityPublic 
//	@EntityShared
//	class TestValidationResult2 extends GenericEntity {
////		public TestValidationResult2() {
////			
////		}
//
//		@EntityUuid
//		@EntityOwnerId
//		@EntityTenantId
//		protected String uuid;
//		
//		@EntityId
//		@EntityUnicity
//		@EntityMandatory
//		protected String id;
//		
//		@AuthenticatorLogin
//		@JsonProperty
//		protected String email;
//		
//		@JsonInclude
//		private String name;
//		
//		@JsonInclude
//		@EntityShare
//		private String surname;
//		
//		@JsonInclude
//		@AuthenticatorPassword
//		@EntityMandatory
//		private String password;
//		
//		@JsonInclude
//		@Setter
//		@AuthenticatorAuthorities
//		private List<String> userAuthorities;
//		
//		@JsonIgnore
//		@AuthenticatorAccountNonExpired
//		@AuthenticatorAccountNonLocked
//		@AuthenticatorCredentialsNonExpired
//		@AuthenticatorEnabled
//		@EntityHidden
//		private boolean enabled = true;
//		
//		@EntitySuperTenant
//		@EntityGotFromRepository
//		private boolean superTenant;
//		
//		@EntitySuperOwner
//		private boolean superOwner;
//		
//		@EntityLocation
//		@EntityUnicity
//		private Point location;
//
//		@Inject
//		@JsonIgnore
//		private IAccessRulesRegistry accessRulesRegistry;
//
//	}
//
//@Entity (
//		domain = "tenants", 
//		creation_access = ServiceAccess.anonymous,
//		count_access = ServiceAccess.tenant,
//		delete_one_access = ServiceAccess.tenant,
//		read_one_access = ServiceAccess.tenant,
//		update_one_access = ServiceAccess.tenant,
//		read_all_access = ServiceAccess.tenant,
//		delete_all_access = ServiceAccess.tenant,
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
//	@Authenticator
//	@EntityTenant
//	@EntityOwned
//	@EntityHiddenable
//	@EntityGeolocalized
//	@EntityPublic 
//	@EntityShared
//	class TestBusinessMethodsPresence  extends GenericEntity {
//
//		@EntityUuid
//		@EntityOwnerId
//		@EntityTenantId
//		protected String uuid;
//		
//		@EntityId
//		@EntityUnicity
//		@EntityMandatory
//		protected String id;
//		
//		@AuthenticatorLogin
//		@JsonProperty
//		protected String email;
//		
//		@JsonInclude
//		private String name;
//		
//		@JsonInclude
//		@EntityShare
//		private String surname;
//		
//		@JsonInclude
//		@AuthenticatorPassword
//		@EntityMandatory
//		private String password;
//		
//		@JsonInclude
//		@Setter
//		@AuthenticatorAuthorities
//		private List<String> userAuthorities;
//		
//		@JsonIgnore
//		@AuthenticatorAccountNonExpired
//		@AuthenticatorAccountNonLocked
//		@AuthenticatorCredentialsNonExpired
//		@AuthenticatorEnabled
//		@EntityHidden
//		private boolean enabled = true;
//		
//		@EntitySuperTenant
//		@EntityGotFromRepository
//		private boolean superTenant;
//		
//		@EntitySuperOwner
//		private boolean superOwner;
//		
//		@EntityLocation
//		@EntityUnicity
//		private Point location;
//
//		@Inject
//		@JsonIgnore
//		private IAccessRulesRegistry accessRulesRegistry;
//		
//		@EntityAfterGet
//		private void afterGet(ICaller caller, Map<String, String> params) {
//		}
//		
//		@EntityBeforeCreate
//		private void beforeCreate(ICaller caller, Map<String, String> params) {
//		}
//		
//		@EntityAfterCreate
//		private void afterCreate(ICaller caller, Map<String, String> params) {
//		}
//		
//		@EntityBeforeUpdate
//		private void beforeUpdate(ICaller caller, Map<String, String> params) {
//		}
//		
//		@EntityAfterUpdate
//		private void afterUpdate(ICaller caller, Map<String, String> params) {
//		}
//		
//		@EntityBeforeDelete
//		private void beforeDelete(ICaller caller, Map<String, String> params) {
//		}
//		
//		@EntityAfterDelete
//		private void afterDelete(ICaller caller, Map<String, String> params) {
//		}
//
//	}
//
//@Entity (
//		domain = "tenants", 
//		creation_access = ServiceAccess.anonymous,
//		count_access = ServiceAccess.tenant,
//		delete_one_access = ServiceAccess.tenant,
//		read_one_access = ServiceAccess.tenant,
//		update_one_access = ServiceAccess.tenant,
//		read_all_access = ServiceAccess.tenant,
//		delete_all_access = ServiceAccess.tenant,
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
//	@Authenticator
//	@EntityTenant
//	@EntityOwned
//	@EntityHiddenable
//	@EntityGeolocalized
//	@EntityPublic 
//	@EntityShared
//	class TestAuthorizeUpdate extends GenericEntity {
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
//		@EntityUuid
//		@EntityOwnerId
//		@EntityTenantId
//		protected String uuid;
//		
//		@EntityId
//		@EntityUnicity
//		@EntityMandatory
//		protected String id;
//		
//		@AuthenticatorLogin
//		@JsonProperty
//		protected String email;
//		
//		@JsonInclude
//		private String name;
//		
//		@JsonInclude
//		@EntityShare
//		private String surname;
//		
//		@JsonInclude
//		@AuthenticatorPassword
//		@EntityMandatory
//		private String password;
//		
//		@JsonInclude
//		@Setter
//		@AuthenticatorAuthorities
//		private List<String> userAuthorities;
//		
//		@JsonIgnore
//		@AuthenticatorAccountNonExpired
//		@AuthenticatorAccountNonLocked
//		@AuthenticatorCredentialsNonExpired
//		@AuthenticatorEnabled
//		@EntityHidden
//		private boolean enabled = true;
//		
//		@EntitySuperTenant
//		@EntityGotFromRepository
//		private boolean superTenant;
//		
//		@EntitySuperOwner
//		@EntityAuthorizeUpdate(authority = "test")
//		private boolean superOwner;
//		
//		@EntityLocation
//		@EntityUnicity
//		private Point location;
//
//		@Inject
//		@JsonIgnore
//		private IAccessRulesRegistry accessRulesRegistry;
//	
//	}
//
//@Entity(
//		domain = "entity"
//)
//class TestAnnotationEntityUuid extends GenericEntity {
//	public TestAnnotationEntityUuid() {
//		
//	}
//	@EntityUuid
//	private String uuid;
//	@EntityId
//	private String id;
//	@EntityGotFromRepository
//	private boolean t;
//	
//}
//
//@Entity(
//		domain = "entity"
//)
//@EntityOwner(ownerId = "uuid", superOwner = "superOwner")
//class TestOwnerEntityWithFieldValue  extends GenericEntity {
////	public TestOwnerEntityWithFieldValue() {
////		
////	}
//	
//	private String uuid;
//	
//	private boolean superOwner;
//	
//	@EntityUuid
//	private String tuuid;
//	
//	@EntityId
//	private String id;
//	@EntityGotFromRepository
//	private boolean t;
//}
//
//@Entity(
//		domain = "entity"
//)
//class TestAnnotationEntityUuidFromSuperClass extends GenericEntity {
////	public TestAnnotationEntityUuidFromSuperClass() {
////		
////	}
//	private String uuid;
//	@EntityGotFromRepository
//	private boolean t;
//	
//}
//
//@Entity(
//		domain = "entity"
//)
//@EntityGeolocalized(location = "location")
//class TestAnnotationGeolocalized extends GenericEntity {
////	public TestAnnotationGeolocalized() {
////		
////	}
//	@EntityGotFromRepository
//	private boolean t;
//	Point location;
//}
//
//@Entity(
//		domain = "entity"
//)
//@EntityOwner
//@EntityOwned
//class TestAnnotationOwnedAndOwner extends GenericEntity{
//	
//}
//
//@Entity(
//		domain = "entity"
//)
//class TestAnnotationEntityIdFromSuperClass extends GenericEntity {
////	public TestAnnotationEntityIdFromSuperClass() {
////		
////	}
//	private String uuid;
//}
//
//@Entity(
//		domain = "entity"
//)
//@EntityOwned(ownerId = "uuid")
//class TestOwnedEntityWithFieldValue  extends GenericEntity {
////	public TestOwnedEntityWithFieldValue() {
////		
////	}
//	private String uuid;
//}
//
//@Entity(
//		domain = "entity"
//)
//class TestAnnotationEntityId extends GenericEntity {
////	public TestAnnotationEntityId() {
////		
////	}
//}
//
//@Entity(
//		domain = "entity"
//)
//@EntityTenant(tenantId = "uuid", superTenant = "superTenant")
//class TestTenantEntityWithFieldValue  extends GenericEntity{
////	public TestTenantEntityWithFieldValue() {
////		
////	}
//	private String uuid;
//	
//	private boolean superTenant;
//	@EntityUuid
//	private String tuuid;
//	@EntityId
//	private String id;
//}
//
//@Entity(
//		domain = "entity"
//)
//@EntityOwner(superOwner = "superOwner")
//class TestOwnerEntityWithAnnotatedField  extends GenericEntity{
////	public TestOwnerEntityWithAnnotatedField() {
////		
////	}
//	@EntityOwnerId
//	private String uuid;
//	
//	private boolean superOwner;
//	
//	@EntityUuid
//	private String tuuid;
//	@EntityId
//	private String id;
//	
//}
//
//@Entity(
//		domain = "entity"
//)
//@EntityTenant(superTenant = "superTenant")
//class TestTenantEntityWithAnnotatedField  extends GenericEntity{
////	public TestTenantEntityWithAnnotatedField() {
////		
////	}
//	@EntityTenantId
//	private String uuid;
//	
//	private boolean superTenant;
//	@EntityUuid
//	private String tuuid;
//	@EntityId
//	private String id;
//	
//}
//
//@Entity(
//		domain = "entity"
//)
//@EntityShared(share = "uuid")
//class TestSharedEntityWithFieldValue  extends GenericEntity {
//	
//	private String uuid;
//}
