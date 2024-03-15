package com.garganttua.api.core.mapper;

import java.lang.reflect.Field;

import org.javatuples.Pair;

import com.garganttua.api.core.mapper.fieldFinder.GGAPIFieldFinderException;

public interface IGGAPIFieldFinder {

	Pair<Field, Class<?>> findField(Class<?> objectClass, String fieldAddress) throws GGAPIFieldFinderException;

}
