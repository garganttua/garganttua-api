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

import com.garganttua.api.core.accessRules.BasicGGAPIAccessRule;
import com.garganttua.api.core.dto.checker.GGAPIDtoChecker;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.entity.checker.GGAPIEntityDocumentationChecker;
import com.garganttua.api.core.security.entity.checker.GGAPIEntitySecurityChecker;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.GGAPIMethod;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.dto.GGAPIDtoInfos;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.api.spec.entity.GGAPIEntityDocumentationInfos;
import com.garganttua.api.spec.entity.GGAPIEntityInfos;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIUnicityScope;
import com.garganttua.api.spec.security.GGAPIEntitySecurityInfos;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.security.annotations.GGAPICustomServiceSecurity;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorScope;
import com.garganttua.api.spec.service.GGAPIServiceAccess;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class GGAPIDomain implements IGGAPIDomain {

	@Getter
	private String domain;
	@Getter
	private Pair<Class<?>, GGAPIEntityInfos> entity;
	@Getter
	private List<Pair<Class<?>, GGAPIDtoInfos>> dtos;
	@Getter
	private GGAPIEntityDocumentationInfos documentation;
	@Getter
	private GGAPIEntitySecurityInfos security;
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
	private Map<GGAPIEntityOperation, IGGAPIServiceInfos> serviceInfos = new HashMap<GGAPIEntityOperation, IGGAPIServiceInfos>();

	@Override
	public void addServicesInfos(List<IGGAPIServiceInfos> servicesInfos) {
		servicesInfos.forEach(info -> {
			this.serviceInfos.put(info.getOperation(), info);
			try {
				this.security.addAccessRule(this.createAccessRule(info));
			} catch (GGAPIEngineException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public void addServiceInfos(IGGAPIServiceInfos servicesInfos) {
		this.serviceInfos.put(servicesInfos.getOperation(), servicesInfos);
		try {
			this.security.addAccessRule(this.createAccessRule(servicesInfos));
		} catch (GGAPIEngineException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public IGGAPIAccessRule createAccessRule(IGGAPIServiceInfos info) throws GGAPIEngineException {
		boolean auth = false;
		GGAPIServiceAccess access = GGAPIServiceAccess.anonymous;
		String authorityLabel = info.getOperation().toString();

		if (info.getOperation().isCustom()) {
			try {
				Method method = info.getInterface().getDeclaredMethod(info.getMethodName(), info.getParameters());
				GGAPICustomServiceSecurity annotation = method.getAnnotation(GGAPICustomServiceSecurity.class);
				access = annotation.access();
				auth = annotation.authority();
			} catch (NoSuchMethodException | SecurityException e) {
				throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR,
						"Error during accesss rule creation for service " + info.toString(), e);
			}
		}
		if (info.getOperation().equals(GGAPIEntityOperation.createOne(this.domain, this.entity.getValue0()))) {
			auth = this.security.isCreationAuthority();
			access = this.security.getCreationAccess();
		}
		if (info.getOperation().equals(GGAPIEntityOperation.deleteAll(this.domain, this.entity.getValue0()))) {
			auth = this.security.isDeleteAllAuthority();
			access = this.security.getDeleteAllAccess();
		}
		if (info.getOperation().equals(GGAPIEntityOperation.deleteOne(this.domain, this.entity.getValue0()))) {
			auth = this.security.isDeleteOneAuthority();
			access = this.security.getDeleteOneAccess();
		}
		if (info.getOperation().equals(GGAPIEntityOperation.readAll(this.domain, this.entity.getValue0()))) {
			auth = this.security.isReadAllAuthority();
			access = this.security.getReadAllAccess();
		}
		if (info.getOperation().equals(GGAPIEntityOperation.readOne(this.domain, this.entity.getValue0()))) {
			auth = this.security.isReadOneAuthority();
			access = this.security.getReadOneAccess();
		}
		if (info.getOperation().equals(GGAPIEntityOperation.updateOne(this.domain, this.entity.getValue0()))) {
			auth = this.security.isUpdateOneAuthority();
			access = this.security.getUpdateOneAccess();
		}
		if (info.getOperation().getMethod().equals(GGAPIMethod.authenticate)) {
			auth = false;
			access = GGAPIServiceAccess.anonymous;
		}
		return new BasicGGAPIAccessRule(info.getPath(), auth ? authorityLabel : null, info.getOperation(), access);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		GGAPIDomain that = (GGAPIDomain) o;
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
		return "GGAPIDomain{" + "domain=" + domain + ", entity=" + entity + ", dtos=" + dtos + ", security= " + security
				+ ", interfaces='" + interfaces + '\'' + ", event='" + event + '\'' + ", allow_creation="
				+ allowCreation + ", allow_read_all=" + allowReadAll + ", allow_read_one=" + allowReadOne
				+ ", allow_update_one=" + allowUpdateOne + ", allow_delete_one=" + allowDeleteOne
				+ ", allow_delete_all=" + allowDeleteAll + '}';
	}

	static public GGAPIDomain fromEntityClass(Class<?> clazz, List<String> scanPackages) throws GGAPIException {
		if (log.isDebugEnabled()) {
			log.debug("Getting domain from class " + clazz.getName());
		}
		Class<?> entityClass = clazz;
		List<Pair<Class<?>, GGAPIDtoInfos>> dtos = new ArrayList<Pair<Class<?>, GGAPIDtoInfos>>();

		GGAPIEntity entityAnnotation = clazz.getAnnotation(GGAPIEntity.class);
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntityClass(entityClass);
		GGAPIEntityDocumentationInfos documentation = GGAPIEntityDocumentationChecker.checkEntityClass(entityClass);
		GGAPIEntitySecurityInfos securityInfos = GGAPIEntitySecurityChecker.checkEntityClass(entityClass,
				infos.domain());

		for (String pack : scanPackages) {
			List<Class<?>> annotatedClasses;
			annotatedClasses = GGObjectReflectionHelper.getClassesWithAnnotation(pack, GGAPIDto.class);
			annotatedClasses.forEach(annotatedClass -> {
				try {
					GGAPIDto dtoAnnotation = annotatedClass.getAnnotation(GGAPIDto.class);
					if (dtoAnnotation.entityClass().equals(entityClass)) {
						GGAPIDtoInfos dtoInfos = GGAPIDtoChecker.checkDtoClass(annotatedClass);
						dtos.add(new Pair<Class<?>, GGAPIDtoInfos>(annotatedClass, dtoInfos));
					}
				} catch (GGAPIException e) {
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

		return new GGAPIDomain(infos.domain(), new Pair<Class<?>, GGAPIEntityInfos>(entityClass, infos), dtos,
				documentation, securityInfos, interfaces, event, allow_creation, allow_read_all, allow_read_one,
				allow_update_one, allow_delete_one, allow_delete_all);
	}

	@Override
	public boolean isTenantIdMandatoryForOperation(GGAPIEntityOperation operation) {
		IGGAPIServiceInfos info = this.serviceInfos.get(operation);
		GGAPIServiceAccess access = this.security.getAccess(info);
		
		if( operation.getMethod() == GGAPIMethod.authenticate && this.security.getAuthenticatorScope() == GGAPIAuthenticatorScope.tenant ) {
			return true;
		}
		
		return (!this.entity.getValue1().publicEntity()
				&& (access == GGAPIServiceAccess.tenant || access == GGAPIServiceAccess.owner));
	}

	@Override
	public boolean isOwnerIdMandatoryForOperation(GGAPIEntityOperation operation) {
		IGGAPIServiceInfos info = this.serviceInfos.get(operation);
		GGAPIServiceAccess access = this.security.getAccess(info);

		if (info.getOperation().equals(GGAPIEntityOperation.createOne(this.domain, this.entity.getValue0()))
				|| info.getOperation().equals(GGAPIEntityOperation.updateOne(this.domain, this.entity.getValue0()))) {
			return this.entity.getValue1().ownedEntity();
		}

		return access == GGAPIServiceAccess.owner && this.entity.getValue1().ownedEntity();
	}

	public GGAPIDomain(String domain, Pair<Class<?>, GGAPIEntityInfos> entity, List<Pair<Class<?>, GGAPIDtoInfos>> dtos,
			GGAPIEntityDocumentationInfos documentation, GGAPIEntitySecurityInfos securityInfos, String[] interfaces,
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
	public Collection<IGGAPIAccessRule> getAccessRules() {
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
	public GGObjectAddress getSuperOnwerIdFieldAddress() {
		return this.entity.getValue1().superOnwerIdFieldAddress();
	}

	@Override
	public GGObjectAddress getSuperTenantFieldAddress() {
		return this.entity.getValue1().superTenantFieldAddress();
	}

	@Override
	public GGObjectAddress getBeforeDeleteMethodAddress() {
		return this.entity.getValue1().beforeDeleteMethodAddress();
	}

	@Override
	public GGObjectAddress getAfterDeleteMethodAddress() {
		return this.entity.getValue1().afterDeleteMethodAddress();
	}

	@Override
	public GGObjectAddress getBeforeCreateMethodAddress() {
		return this.entity.getValue1().beforeCreateMethodAddress();
	}

	@Override
	public GGObjectAddress getAfterCreateMethodAddress() {
		return this.entity.getValue1().afterCreateMethodAddress();
	}

	@Override
	public GGObjectAddress getBeforeUpdateMethodAddress() {
		return this.entity.getValue1().beforeUpdateMethodAddress();
	}

	@Override
	public GGObjectAddress getAfterUpdateMethodAddress() {
		return this.entity.getValue1().afterUpdateMethodAddress();
	}

	@Override
	public GGObjectAddress getOwnerIdFieldAddress() {
		return this.entity.getValue1().ownerIdFieldAddress();
	}

	@Override
	public GGObjectAddress getAfterGetMethodAddress() {
		return this.entity.getValue1().afterGetMethodAddress();
	}

	@Override
	public GGObjectAddress getShareFieldAddress() {
		return this.entity.getValue1().shareFieldAddress();
	}

	@Override
	public GGObjectAddress getTenantIdFieldAddress() {
		return this.entity.getValue1().tenantIdFieldAddress();
	}

	@Override
	public GGObjectAddress getHiddenFieldAddress() {
		return this.entity.getValue1().hiddenFieldAddress();
	}

	@Override
	public GGObjectAddress getUuidFieldAddress() {
		return this.entity.getValue1().uuidFieldAddress();
	}

	@Override
	public GGObjectAddress getIdFieldAddress() {
		return this.entity.getValue1().idFieldAddress();
	}

	@Override
	public Map<GGObjectAddress, String> getAuthorizedUpdateFieldsAndAuthorizations() {
		return this.entity.getValue1().updateAuthorizations();
	}

	@Override
	public List<Pair<GGObjectAddress, GGAPIUnicityScope>> getUnicityFields() {
		return this.entity.getValue1().unicityFields();
	}

	@Override
	public List<GGObjectAddress> getMandatoryFields() {
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
	public GGObjectAddress getLocationFieldAddress() {
		return this.entity.getValue1().locationFieldAddress();
	}

	@Override
	public GGAPIServiceAccess getAccess(IGGAPIServiceInfos info) {
		return this.security.getAccess(info);
	}

	@Override
	public String getAuthority(IGGAPIServiceInfos info) {
		return this.security.getAuthority(info);
	}

	@Override
	public GGAPIAuthenticatorScope getAuthenticatorScope() {
		// TODO Auto-generated method stub
		return this.security.getAuthenticatorScope();
	}
}
