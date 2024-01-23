package com.garganttua.api.core;

public interface IGGAPIEntityFactory<T extends IGGAPIEntity> {
	
	T newInstance();

	T newInstance(String uuid);

}
