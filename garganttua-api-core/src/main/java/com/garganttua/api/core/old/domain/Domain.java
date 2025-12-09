package com.garganttua.api.core.domain;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import com.garganttua.api.core.accessRules.BasicAccessRule;
import com.garganttua.api.core.dto.checker.DtoChecker;
import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.entity.checker.EntityChecker;
import com.garganttua.api.core.entity.checker.EntityDocumentationChecker;
import com.garganttua.api.core.security.entity.checker.EntitySecurityChecker;
import com.garganttua.api.spec.EntityOperation;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.dto.DtoInfos;
import com.garganttua.api.spec.dto.annotations.Dto;
import com.garganttua.api.spec.entity.EntityDocumentationInfos;
import com.garganttua.api.spec.entity.EntityInfos;
import com.garganttua.api.spec.entity.annotations.Entity;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.api.spec.security.EntitySecurityInfos;
import com.garganttua.api.spec.security.IAccessRule;
import com.garganttua.api.spec.security.annotations.CustomServiceSecurity;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.api.spec.service.ServiceAccess;
import com.garganttua.api.spec.service.IServiceInfos;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.utils.GGObjectReflectionHelper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class Domain implements IDomain {

	@Getter
	private String domain;
	@Getter
	private Pair<Class<?>, EntityInfos> entity;
	@Getter
	private List<Pair<Class<?>, DtoInfos>> dtos;
	@Getter
	private EntityDocumentationInfos documentation;
	@Getter
	private EntitySecurityInfos security;
	@Getter
	private String[] interfaces;
	@Getter
	private String event;
	@Getter
	private boolean allowCreation;
	@Getter
	private boolean allowReadAll;
	@Getter
	private boolean allowReadOne;
	@Getter
	private boolean allowUpdateOne;
	@Getter
	private boolean allowDeleteOne;
	@Getter
	private boolean allowDeleteAll;
	@Getter
	private Map<EntityOperation, IServiceInfos> serviceInfos = new HashMap<EntityOperation, IServiceInfos>();

	@Override
	public void addServicesInfos(List<IServiceInfos> servicesInfos) {
		servicesInfos.forEach(info -> {
			this.serviceInfos.put(info.getOperation(), info);
			try {
				this.security.addAccessRule(this.createAccessRule(info));
			} catch (EngineException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public void addServiceInfos(IServiceInfos servicesInfos) {
		this.serviceInfos.put(servicesInfos.getOperation(), servicesInfos);
		try {
			this.security.addAccessRule(this.createAccessRule(servicesInfos));
		} catch (EngineException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public IAccessRule createAccessRule(IServiceInfos info) throws EngineException {
		boolean auth = false;
		ServiceAccess access = ServiceAccess.anonymous;
		String authorityLabel = info.getOperation().toString();

		if (info.getOperation().isCustom()) {
			try {
				Method method = info.getInterface().getDeclaredMethod(info.getMethodName(), info.getParameters());
				CustomServiceSecurity annotation = method.getAnnotation(CustomServiceSecurity.class);
				access = annotation.access();
				auth = annotation.authority();
			} catch (NoSuchMethodException | SecurityException e) {
				throw new EngineException(CoreExceptionCode.UNKNOWN_ERROR,
						"Error during accesss rule creation for service " + info.toString(), e);
			}
		}
		if (info.getOperation().equals(EntityOperation.createOne(this.domain, this.entity.getValue0()))) {
			auth = this.security.isCreationAuthority();
			access = this.security.getCreationAccess();
		}
		if (info.getOperation().equals(EntityOperation.deleteAll(this.domain, this.entity.getValue0()))) {
			auth = this.security.isDeleteAllAuthority();
			access = this.security.getDeleteAllAccess();
		}
		if (info.getOperation().equals(EntityOperation.deleteOne(this.domain, this.entity.getValue0()))) {
			auth = this.security.isDeleteOneAuthority();
			access = this.security.getDeleteOneAccess();
		}
		if (info.getOperation().equals(EntityOperation.readAll(this.domain, this.entity.getValue0()))) {
			auth = this.security.isReadAllAuthority();
			access = this.security.getReadAllAccess();
		}
		if (info.getOperation().equals(EntityOperation.readOne(this.domain, this.entity.getValue0()))) {
			auth = this.security.isReadOneAuthority();
			access = this.security.getReadOneAccess();
		}
		if (info.getOperation().equals(EntityOperation.updateOne(this.domain, this.entity.getValue0()))) {
			auth = this.security.isUpdateOneAuthority();
			access = this.security.getUpdateOneAccess();
		}
		if (info.getOperation().getMethod().equals(com.garganttua.api.spec.Method.authenticate)) {
			auth = false;
			access = ServiceAccess.anonymous;
		}
		return new BasicAccessRule(info.getPath(), auth ? authorityLabel : null, info.getOperation(), access);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Domain that = (Domain) o;
		return allowCreation == that.allowCreation && allowReadAll == that.allowReadAll
				&& allowReadOne == that.allowReadOne && allowUpdateOne == that.allowUpdateOne
				&& allowDeleteOne == that.allowDeleteOne && allowDeleteAll == that.allowDeleteAll
				&& Objects.equals(entity, that.entity) && Objects.equals(dtos, that.dtos)
				&& Objects.equals(security, that.security) && Objects.equals(interfaces, that.interfaces)
				&& Objects.equals(event, that.event);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entity, dtos, security, interfaces, event, allowCreation, allowReadAll, allowReadOne,
				allowUpdateOne, allowDeleteOne, allowDeleteAll);
	}

	@Override
	public String toString() {
		return "Domain{" + "domain=" + domain + ", entity=" + entity + ", dtos=" + dtos + ", security= " + security
				+ ", interfaces='" + interfaces + '\'' + ", event='" + event + '\'' + ", allow_creation="
				+ allowCreation + ", allow_read_all=" + allowReadAll + ", allow_read_one=" + allowReadOne
				+ ", allow_update_one=" + allowUpdateOne + ", allow_delete_one=" + allowDeleteOne
				+ ", allow_delete_all=" + allowDeleteAll + '}';
	}

	static public Domain fromEntityClass(Class<?> clazz, List<String> scanPackages) throws CoreException {
		if (log.isDebugEnabled()) {
			log.debug("Getting domain from class " + clazz.getName());
		}
		Class<?> entityClass = clazz;
		List<Pair<Class<?>, DtoInfos>> dtos = new ArrayList<Pair<Class<?>, DtoInfos>>();

		Entity entityAnnotation = clazz.getAnnotation(Entity.class);
		EntityInfos infos = EntityChecker.checkEntityClass(entityClass);
		EntityDocumentationInfos documentation = EntityDocumentationChecker.checkEntityClass(entityClass);
		EntitySecurityInfos securityInfos = EntitySecurityChecker.checkEntityClass(entityClass,
				infos.domain());

		for (String pack : scanPackages) {
			List<Class<?>> annotatedClasses;
			annotatedClasses = GGObjectReflectionHelper.getClassesWithAnnotation(pack, Dto.class);
			annotatedClasses.forEach(annotatedClass -> {
				try {
					Dto dtoAnnotation = annotatedClass.getAnnotation(Dto.class);
					if (dtoAnnotation.entityClass().equals(entityClass)) {
						DtoInfos dtoInfos = DtoChecker.checkDtoClass(annotatedClass);
						dtos.add(new Pair<Class<?>, DtoInfos>(annotatedClass, dtoInfos));
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			});
		}

		boolean allow_creation = entityAnnotation.allow_creation();
		boolean allow_read_all = entityAnnotation.allow_read_all();
		boolean allow_read_one = entityAnnotation.allow_read_one();
		boolean allow_update_one = entityAnnotation.allow_update_one();
		boolean allow_delete_one = entityAnnotation.allow_delete_one();
		boolean allow_delete_all = entityAnnotation.allow_delete_all();

		String event = entityAnnotation.eventPublisher();
		String[] interfaces = entityAnnotation.interfaces();

		return new Domain(infos.domain(), new Pair<Class<?>, EntityInfos>(entityClass, infos), dtos,
				documentation, securityInfos, interfaces, event, allow_creation, allow_read_all, allow_read_one,
				allow_update_one, allow_delete_one, allow_delete_all);
	}

	@Override
	public boolean isTenantIdMandatoryForOperation(EntityOperation operation) {
		IServiceInfos info = this.serviceInfos.get(operation);
		ServiceAccess access = this.security.getAccess(info);
		
		if( operation.getMethod() == com.garganttua.api.spec.Method.authenticate && this.security.getAuthenticatorScope() == AuthenticatorScope.tenant ) {
			return true;
		}
		
		return (!this.entity.getValue1().publicEntity()
				&& (access == ServiceAccess.tenant || access == ServiceAccess.owner));
	}

	@Override
	public boolean isOwnerIdMandatoryForOperation(EntityOperation operation) {
		IServiceInfos info = this.serviceInfos.get(operation);
		ServiceAccess access = this.security.getAccess(info);

		if (info.getOperation().equals(EntityOperation.createOne(this.domain, this.entity.getValue0()))
				|| info.getOperation().equals(EntityOperation.updateOne(this.domain, this.entity.getValue0()))) {
			return this.entity.getValue1().ownedEntity();
		}

		return access == ServiceAccess.owner && this.entity.getValue1().ownedEntity();
	}

	public Domain(String domain, Pair<Class<?>, EntityInfos> entity, List<Pair<Class<?>, DtoInfos>> dtos,
			EntityDocumentationInfos documentation, EntitySecurityInfos securityInfos, String[] interfaces,
			String event, boolean allow_creation, boolean allow_read_all, boolean allow_read_one,
			boolean allow_update_one, boolean allow_delete_one, boolean allow_delete_all) {
		this.domain = domain;
		this.dtos = dtos;
		this.entity = entity;
		this.documentation = documentation;
		this.security = securityInfos;
		this.interfaces = interfaces;
		this.event = event;
		this.allowCreation = allow_creation;
		this.allowReadAll = allow_read_all;
		this.allowReadOne = allow_read_one;
		this.allowUpdateOne = allow_update_one;
		this.allowDeleteOne = allow_delete_one;
		this.allowDeleteAll = allow_delete_all;
	}

	@Override
	public String getEntityName() {
		return this.entity.getValue0().getSimpleName().toLowerCase();
	}

	@Override
	public Collection<IAccessRule> getAccessRules() {
		return this.security.getAccessRules().values();
	}

	@Override
	public Collection<String> getUpdateAuthorizations() {
		return this.entity.getValue1().updateAuthorizations().values().stream().map(val -> {return val;}).collect(Collectors.toList());
	}

	@Override
	public Class<?> getEntityClass() {
		return this.entity.getValue0();
	}

	@Override
	public boolean isOwnerEntity() {
		return this.entity.getValue1().ownerEntity();
	}

	@Override
	public boolean isTenantEntity() {
		return this.entity.getValue1().tenantEntity();
	}

	@Override
	public boolean isOwnedEntity() {
		return this.entity.getValue1().ownedEntity();
	}

	@Override
	public boolean isSharedEntity() {
		return this.entity.getValue1().sharedEntity();
	}

	@Override
	public boolean isHiddenableEntity() {
		return this.entity.getValue1().hiddenableEntity();
	}

	@Override
	public boolean isPublicEntity() {
		return this.entity.getValue1().publicEntity();
	}

	@Override
	public ObjectAddress getSuperOnwerIdFieldAddress() {
		return this.entity.getValue1().superOnwerIdFieldAddress();
	}

	@Override
	public ObjectAddress getSuperTenantFieldAddress() {
		return this.entity.getValue1().superTenantFieldAddress();
	}

	@Override
	public ObjectAddress getBeforeDeleteMethodAddress() {
		return this.entity.getValue1().beforeDeleteMethodAddress();
	}

	@Override
	public ObjectAddress getAfterDeleteMethodAddress() {
		return this.entity.getValue1().afterDeleteMethodAddress();
	}

	@Override
	public ObjectAddress getBeforeCreateMethodAddress() {
		return this.entity.getValue1().beforeCreateMethodAddress();
	}

	@Override
	public ObjectAddress getAfterCreateMethodAddress() {
		return this.entity.getValue1().afterCreateMethodAddress();
	}

	@Override
	public ObjectAddress getBeforeUpdateMethodAddress() {
		return this.entity.getValue1().beforeUpdateMethodAddress();
	}

	@Override
	public ObjectAddress getAfterUpdateMethodAddress() {
		return this.entity.getValue1().afterUpdateMethodAddress();
	}

	@Override
	public ObjectAddress getOwnerIdFieldAddress() {
		return this.entity.getValue1().ownerIdFieldAddress();
	}

	@Override
	public ObjectAddress getAfterGetMethodAddress() {
		return this.entity.getValue1().afterGetMethodAddress();
	}

	@Override
	public ObjectAddress getShareFieldAddress() {
		return this.entity.getValue1().shareFieldAddress();
	}

	@Override
	public ObjectAddress getTenantIdFieldAddress() {
		return this.entity.getValue1().tenantIdFieldAddress();
	}

	@Override
	public ObjectAddress getHiddenFieldAddress() {
		return this.entity.getValue1().hiddenFieldAddress();
	}

	@Override
	public ObjectAddress getUuidFieldAddress() {
		return this.entity.getValue1().uuidFieldAddress();
	}

	@Override
	public ObjectAddress getIdFieldAddress() {
		return this.entity.getValue1().idFieldAddress();
	}

	@Override
	public Map<ObjectAddress, String> getAuthorizedUpdateFieldsAndAuthorizations() {
		return this.entity.getValue1().updateAuthorizations();
	}

	@Override
	public List<Pair<ObjectAddress, UnicityScope>> getUnicityFields() {
		return this.entity.getValue1().unicityFields();
	}

	@Override
	public List<ObjectAddress> getMandatoryFields() {
		return this.entity.getValue1().mandatoryFields();
	}

	@Override
	public boolean isAuthenticatorEntity() {
		return this.security.isAuthenticatorEntity();
	}

	@Override
	public Collection<Class<?>> getAuthorizationProtocols() {
		return List.of(this.security.getAuthorizationProtocols());
	}

	@Override
	public Collection<Class<?>> getAuthorizations() {
		return List.of(this.security.getAuthorizations());
	}

	@Override
	public boolean isGeolocalizedEntity() {
		return this.entity.getValue1().geolocalizedEntity();
	}

	@Override
	public ObjectAddress getLocationFieldAddress() {
		return this.entity.getValue1().locationFieldAddress();
	}

	@Override
	public ServiceAccess getAccess(IServiceInfos info) {
		return this.security.getAccess(info);
	}

	@Override
	public String getAuthority(IServiceInfos info) {
		return this.security.getAuthority(info);
	}

	@Override
	public AuthenticatorScope getAuthenticatorScope() {
		// TODO Auto-generated method stub
		return this.security.getAuthenticatorScope();
	}
}
