package com.garganttua.api.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.javatuples.Pair;
import org.reflections.Reflections;

import com.garganttua.api.core.GGAPIServiceAccess;
import com.garganttua.api.core.dto.annotations.GGAPIDto;
import com.garganttua.api.core.dto.checker.GGAPIDtoChecker;
import com.garganttua.api.core.dto.checker.GGAPIDtoChecker.GGAPIDtoInfos;
import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker.GGAPIEntityInfos;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class GGAPIDomain {
	
	public Pair<Class<?>, GGAPIEntityInfos> entity;
	public List<Pair<Class<?>, GGAPIDtoInfos>> dtos;
	public String ws;
	public String event;
	public String repo;
	public boolean allow_creation;
	public boolean allow_read_all;
	public boolean allow_read_one;
	public boolean allow_update_one;
	public boolean allow_delete_one;
	public boolean allow_delete_all;
	public boolean allow_count;
	public GGAPIServiceAccess creation_access;
	public GGAPIServiceAccess read_all_access;
	public GGAPIServiceAccess read_one_access;
	public GGAPIServiceAccess update_one_access;
	public GGAPIServiceAccess delete_one_access;
	public GGAPIServiceAccess delete_all_access;
	public GGAPIServiceAccess count_access;
	public boolean creation_authority;
	public boolean read_all_authority;
	public boolean read_one_authority;
	public boolean update_one_authority;
	public boolean delete_one_authority;
	public boolean delete_all_authority;
	public boolean count_authority;

	 @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;
	        GGAPIDomain that = (GGAPIDomain) o;
	        return allow_creation == that.allow_creation &&
	                allow_read_all == that.allow_read_all &&
	                allow_read_one == that.allow_read_one &&
	                allow_update_one == that.allow_update_one &&
	                allow_delete_one == that.allow_delete_one &&
	                allow_delete_all == that.allow_delete_all &&
	                allow_count == that.allow_count &&
	                creation_authority == that.creation_authority &&
	                read_all_authority == that.read_all_authority &&
	                read_one_authority == that.read_one_authority &&
	                update_one_authority == that.update_one_authority &&
	                delete_one_authority == that.delete_one_authority &&
	                delete_all_authority == that.delete_all_authority &&
	                count_authority == that.count_authority &&
	                Objects.equals(entity, that.entity) &&
	                Objects.equals(dtos, that.dtos) &&
	                Objects.equals(ws, that.ws) &&
	                Objects.equals(event, that.event) &&
	                Objects.equals(repo, that.repo) &&
	                Objects.equals(creation_access, that.creation_access) &&
	                Objects.equals(read_all_access, that.read_all_access) &&
	                Objects.equals(read_one_access, that.read_one_access) &&
	                Objects.equals(update_one_access, that.update_one_access) &&
	                Objects.equals(delete_one_access, that.delete_one_access) &&
	                Objects.equals(delete_all_access, that.delete_all_access) &&
	                Objects.equals(count_access, that.count_access);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(entity, dtos, ws, event, repo, allow_creation, allow_read_all, allow_read_one,
	                allow_update_one, allow_delete_one, allow_delete_all, allow_count, creation_access, read_all_access,
	                read_one_access, update_one_access, delete_one_access, delete_all_access, count_access, creation_authority,
	                read_all_authority, read_one_authority, update_one_authority, delete_one_authority, delete_all_authority,
	                count_authority);
	    }

	    @Override
	    public String toString() {
	        return "GGAPIDomain{" +
	                "entity=" + entity +
	                ", dtos=" + dtos +
	                ", ws='" + ws + '\'' +
	                ", event='" + event + '\'' +
	                ", repo='" + repo + '\'' +
	                ", allow_creation=" + allow_creation +
	                ", allow_read_all=" + allow_read_all +
	                ", allow_read_one=" + allow_read_one +
	                ", allow_update_one=" + allow_update_one +
	                ", allow_delete_one=" + allow_delete_one +
	                ", allow_delete_all=" + allow_delete_all +
	                ", allow_count=" + allow_count +
	                ", creation_access=" + creation_access +
	                ", read_all_access=" + read_all_access +
	                ", read_one_access=" + read_one_access +
	                ", update_one_access=" + update_one_access +
	                ", delete_one_access=" + delete_one_access +
	                ", delete_all_access=" + delete_all_access +
	                ", count_access=" + count_access +
	                ", creation_authority=" + creation_authority +
	                ", read_all_authority=" + read_all_authority +
	                ", read_one_authority=" + read_one_authority +
	                ", update_one_authority=" + update_one_authority +
	                ", delete_one_authority=" + delete_one_authority +
	                ", delete_all_authority=" + delete_all_authority +
	                ", count_authority=" + count_authority +
	                '}';
	    }

	static public GGAPIDomain fromEntityClass(Class<?> clazz, String[] scanPackages) throws GGAPIEntityException, GGAPIDtoException {
		if (log.isDebugEnabled()) {
			log.debug("Getting dynamic domain from class " + clazz.getName());
		}
		Class<?> entityClass = clazz;
		List<Pair<Class<?>, GGAPIDtoInfos>> dtos = new ArrayList<Pair<Class<?>,GGAPIDtoInfos>>();
		
		GGAPIEntity entityAnnotation = clazz.getAnnotation(GGAPIEntity.class);
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntityClass(entityClass);
		
		for (String pack : scanPackages) {
			Reflections reflections = new Reflections(pack);
			Set<Class<?>> dtos__ = reflections.getTypesAnnotatedWith(GGAPIDto.class, false);
			for( Class<?> dtoClass: dtos__) {
				GGAPIDto dtoAnnotation = dtoClass.getAnnotation(GGAPIDto.class);
				if( dtoAnnotation.entityClass().equals(entityClass) ) {
					GGAPIDtoInfos dtoInfos = GGAPIDtoChecker.checkDto(dtoClass);
					dtos.add(new Pair<Class<?>, GGAPIDtoChecker.GGAPIDtoInfos>(dtoClass, dtoInfos));
				}
			}
		}
		
		boolean allow_creation = entityAnnotation.allow_creation();
		boolean allow_read_all = entityAnnotation.allow_read_all();
		boolean allow_read_one = entityAnnotation.allow_read_one();
		boolean allow_update_one = entityAnnotation.allow_update_one();
		boolean allow_delete_one = entityAnnotation.allow_delete_one();
		boolean allow_delete_all = entityAnnotation.allow_delete_all();
		boolean allow_count = entityAnnotation.allow_count();

		GGAPIServiceAccess creation_access = entityAnnotation.creation_access();
		GGAPIServiceAccess read_all_access = entityAnnotation.read_all_access();
		GGAPIServiceAccess read_one_access = entityAnnotation.read_one_access();
		GGAPIServiceAccess update_one_access = entityAnnotation.update_one_access();
		GGAPIServiceAccess delete_one_access = entityAnnotation.delete_one_access();
		GGAPIServiceAccess delete_all_access = entityAnnotation.delete_all_access();
		GGAPIServiceAccess count_access = entityAnnotation.count_access();

		boolean creation_authority = entityAnnotation.creation_authority();
		boolean read_all_authority = entityAnnotation.read_all_authority();
		boolean read_one_authority = entityAnnotation.read_one_authority();
		boolean update_one_authority = entityAnnotation.update_one_authority();
		boolean delete_one_authority = entityAnnotation.delete_one_authority();
		boolean delete_all_authority = entityAnnotation.delete_all_authority();
		boolean count_authority = entityAnnotation.count_authority();

		String ws = entityAnnotation.ws();
		String event = entityAnnotation.eventPublisher();
		String repo = entityAnnotation.repository();

		return new GGAPIDomain(new Pair<Class<?>, GGAPIEntityInfos> (entityClass, infos), dtos, ws, event, repo, allow_creation,
				allow_read_all, allow_read_one, allow_update_one, allow_delete_one, allow_delete_all, allow_count,
				creation_access, read_all_access, read_one_access, update_one_access, delete_one_access,
				delete_all_access, count_access, creation_authority, read_all_authority, read_one_authority,
				update_one_authority, delete_one_authority, delete_all_authority, count_authority);
	}

}
