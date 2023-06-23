package com.garganttua.api.spec;

public interface ISpringCrudifyEntityFactory<T extends ISpringCrudifyEntity> {
	
	T newInstance();

	T newInstance(String uuid);

}
