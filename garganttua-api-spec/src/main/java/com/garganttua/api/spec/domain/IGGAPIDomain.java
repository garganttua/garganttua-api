package com.garganttua.api.spec.domain;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.dto.GGAPIDtoInfos;
import com.garganttua.api.spec.entity.GGAPIEntityInfos;
import com.garganttua.api.spec.security.GGAPIEntitySecurityInfos;

public interface IGGAPIDomain {

	String getDomain();

	Pair<Class<?>, GGAPIEntityInfos> getEntity();

	List<Pair<Class<?>, GGAPIDtoInfos>> getDtos();
	
	GGAPIEntitySecurityInfos getSecurity();

	String[] getInterfaces();

	String getEvent();

	boolean isAllowCreation();

	boolean isAllowReadAll();

	boolean isAllowReadOne();

	boolean isAllowUpdateOne();

	boolean isAllowDeleteOne();

	boolean isAllowDeleteAll();

	boolean isAllowCount();

}
