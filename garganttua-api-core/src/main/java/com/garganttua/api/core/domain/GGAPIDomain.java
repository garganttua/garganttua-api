package com.garganttua.api.core.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.javatuples.Pair;

import com.garganttua.api.core.dto.checker.GGAPIDtoChecker;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.dto.GGAPIDtoInfos;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.api.spec.entity.GGAPIEntityInfos;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
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
	private String[] interfaces;
	@Getter
	private String[] daos;
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
	private boolean allowCount;

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
				&& allowCount == that.allowCount && Objects.equals(entity, that.entity)
				&& Objects.equals(dtos, that.dtos) && Objects.equals(interfaces, that.interfaces)
				&& Objects.equals(event, that.event) && Objects.equals(daos, that.daos);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entity, dtos, interfaces, event, daos, allowCreation, allowReadAll, allowReadOne,
				allowUpdateOne, allowDeleteOne, allowDeleteAll, allowCount);
	}

	@Override
	public String toString() {
		return "GGAPIDomain{" + "domain=" + domain + ", entity=" + entity + ", dtos=" + dtos + ", ws='" + interfaces
				+ '\'' + ", event='" + event + '\'' + ", repo='" + daos + '\'' + ", allow_creation=" + allowCreation
				+ ", allow_read_all=" + allowReadAll + ", allow_read_one=" + allowReadOne + ", allow_update_one="
				+ allowUpdateOne + ", allow_delete_one=" + allowDeleteOne + ", allow_delete_all=" + allowDeleteAll
				+ ", allow_count=" + allowCount + '}';
	}

	static public GGAPIDomain fromEntityClass(Class<?> clazz, List<String> scanPackages) throws GGAPIException {
		if (log.isDebugEnabled()) {
			log.debug("Getting dynamic domain from class " + clazz.getName());
		}
		Class<?> entityClass = clazz;
		List<Pair<Class<?>, GGAPIDtoInfos>> dtos = new ArrayList<Pair<Class<?>, GGAPIDtoInfos>>();

		GGAPIEntity entityAnnotation = clazz.getAnnotation(GGAPIEntity.class);
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntityClass(entityClass);

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
		boolean allow_count = entityAnnotation.allow_count();

		String event = entityAnnotation.eventPublisher();
		String[] daos = entityAnnotation.daos();
		String[] interfaces = entityAnnotation.interfaces();

		return new GGAPIDomain(infos.domain(), new Pair<Class<?>, GGAPIEntityInfos>(entityClass, infos), dtos,
				interfaces, daos, event, allow_creation, allow_read_all, allow_read_one, allow_update_one,
				allow_delete_one, allow_delete_all, allow_count);
	}
}
