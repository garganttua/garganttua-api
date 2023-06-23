package com.garganttua.api.spec;

public interface IGGAPIEntityFactory<T extends IGGAPIEntity> {
	
	T newInstance();

	T newInstance(String uuid);

}
