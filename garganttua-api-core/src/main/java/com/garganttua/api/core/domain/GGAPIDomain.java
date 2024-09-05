package com.garganttua.api.core.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.javatuples.Pair;

import com.garganttua.api.core.dto.checker.GGAPIDtoChecker;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.security.core.entity.checker.GGAPIEntitySecurityChecker;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.dto.GGAPIDtoInfos;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.api.spec.entity.GGAPIEntityInfos;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.security.GGAPIEntitySecurityInfos;
import com.garganttua.api.spec.service.GGAPIServiceAccess;
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
				&& Objects.equals(entity, that.entity)
				&& Objects.equals(dtos, that.dtos) && Objects.equals(security, that.security) && Objects.equals(interfaces, that.interfaces)
				&& Objects.equals(event, that.event);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entity, dtos, security, interfaces, event, allowCreation, allowReadAll, allowReadOne,
				allowUpdateOne, allowDeleteOne, allowDeleteAll);
	}

	@Override
	public String toString() {
		return "GGAPIDomain{" + "domain=" + domain + ", entity=" + entity + ", dtos=" + dtos + ", security= "+security+", interfaces='" + interfaces
				+ '\'' + ", event='" + event + '\'' + ", allow_creation=" + allowCreation
				+ ", allow_read_all=" + allowReadAll + ", allow_read_one=" + allowReadOne + ", allow_update_one="
				+ allowUpdateOne + ", allow_delete_one=" + allowDeleteOne + ", allow_delete_all=" + allowDeleteAll + '}';
	}

	static public GGAPIDomain fromEntityClass(Class<?> clazz, List<String> scanPackages) throws GGAPIException {
		if (log.isDebugEnabled()) {
			log.debug("Getting domain from class " + clazz.getName());
		}
		Class<?> entityClass = clazz;
		List<Pair<Class<?>, GGAPIDtoInfos>> dtos = new ArrayList<Pair<Class<?>, GGAPIDtoInfos>>();

		GGAPIEntity entityAnnotation = clazz.getAnnotation(GGAPIEntity.class);
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntityClass(entityClass);
		GGAPIEntitySecurityInfos securityInfos = GGAPIEntitySecurityChecker.checkEntityClass(entityClass);

		for (String pack : scanPackages) {
			List<Class<?>> annotatedClasses;
			try {
				annotatedClasses = GGObjectReflectionHelper.getClassesWithAnnotation(pack, GGAPIDto.class);
				annotatedClasses.forEach(annotatedClass -> {
					try {
						GGAPIDto dtoAnnotation = annotatedClass.getAnnotation(GGAPIDto.class);
						if (dtoAnnotation.entityClass().equals(entityClass)) {
							GGAPIDtoInfos dtoInfos = GGAPIDtoChecker.checkDto(annotatedClass);
							dtos.add(new Pair<Class<?>, GGAPIDtoInfos>(annotatedClass, dtoInfos));
						}
					} catch (GGAPIException e) {
						e.printStackTrace();
					}
				});
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}

		boolean allow_creation = entityAnnotation.allow_creation();
		boolean allow_read_all = entityAnnotation.allow_read_all();
		boolean allow_read_one = entityAnnotation.allow_read_one();
		boolean allow_update_one = entityAnnotation.allow_update_one();
		boolean allow_delete_one = entityAnnotation.allow_delete_one();
		boolean allow_delete_all = entityAnnotation.allow_delete_all();

		String event = entityAnnotation.eventPublisher();
		String[] interfaces = entityAnnotation.interfaces();

		return new GGAPIDomain(infos.domain(), new Pair<Class<?>, GGAPIEntityInfos>(entityClass, infos), dtos, securityInfos,
				interfaces, event, allow_creation, allow_read_all, allow_read_one, allow_update_one,
				allow_delete_one, allow_delete_all);
	}

	@Override
	public boolean isTenantIdMandatoryForOperation(GGAPIEntityOperation operation) {
		boolean mandatory = true;
		switch (operation) {
		case read_all: 
			mandatory = ( !this.entity.getValue1().publicEntity() && ( this.security.readAllAccess() == GGAPIServiceAccess.tenant || this.security.readAllAccess() == GGAPIServiceAccess.owner ) );
			break;
		case create_one:
			mandatory = ( !this.entity.getValue1().publicEntity() && ( this.security.creationAccess() == GGAPIServiceAccess.tenant || this.security.creationAccess() == GGAPIServiceAccess.owner ) );
			break;
		case delete_all:
			mandatory = ( !this.entity.getValue1().publicEntity() && ( this.security.deleteAllAccess() == GGAPIServiceAccess.tenant || this.security.deleteAllAccess() == GGAPIServiceAccess.owner ) );
			break;
		case delete_one:
			mandatory = ( !this.entity.getValue1().publicEntity() && ( this.security.deleteOneAccess() == GGAPIServiceAccess.tenant || this.security.deleteOneAccess() == GGAPIServiceAccess.owner ) );
			break;
		case read_one:
			mandatory = ( !this.entity.getValue1().publicEntity() && ( this.security.readOneAccess() == GGAPIServiceAccess.tenant || this.security.readOneAccess() == GGAPIServiceAccess.owner ) );
			break;
		case update_one:
			mandatory = ( !this.entity.getValue1().publicEntity() && ( this.security.updateOneAccess() == GGAPIServiceAccess.tenant || this.security.updateOneAccess() == GGAPIServiceAccess.owner ) );
			break;
		default:
			mandatory = true;
			break;
		}

		return mandatory;
	}

	@Override
	public boolean isOwnerIdMandatoryForOperation(GGAPIEntityOperation operation) {
		boolean mandatory = true;
		switch (operation) {
		case read_all: 
			mandatory = this.security.readAllAccess() == GGAPIServiceAccess.owner && this.entity.getValue1().ownedEntity();
			break;
		case create_one:
			mandatory = this.entity.getValue1().ownedEntity();
			break;
		case delete_all:
			mandatory = this.security.deleteAllAccess() == GGAPIServiceAccess.owner && this.entity.getValue1().ownedEntity();
			break;
		case delete_one:
			mandatory = this.security.deleteOneAccess() == GGAPIServiceAccess.owner && this.entity.getValue1().ownedEntity();
			break;
		case read_one:
			mandatory = this.security.readOneAccess() == GGAPIServiceAccess.owner && this.entity.getValue1().ownedEntity();
			break;
		case update_one:
			mandatory = this.entity.getValue1().ownedEntity();
			break;
		default:
			mandatory = true;
			break;
		}

		return mandatory;
	}
}
