package com.garganttua.api.core.objects.query;

import java.util.List;

import com.garganttua.api.core.objects.GGAPIObjectAddress;

public interface IGGAPIObjectQuery {

	List<Object> find(GGAPIObjectAddress address) throws GGAPIObjectQueryException;
	List<Object> find(String address) throws GGAPIObjectQueryException;
	
	GGAPIObjectAddress address(String elementName) throws GGAPIObjectQueryException;
	
	Object fieldValueStructure(GGAPIObjectAddress address) throws GGAPIObjectQueryException;
	Object fieldValueStructure(String address) throws GGAPIObjectQueryException;

	Object setValue(Object object, String fieldAddress, Object fieldValue) throws GGAPIObjectQueryException;
	Object setValue(Object object, GGAPIObjectAddress fieldAddress, Object fieldValue) throws GGAPIObjectQueryException;
	Object setValue(String fieldAddress, Object fieldValue) throws GGAPIObjectQueryException;
	Object setValue(GGAPIObjectAddress fieldAddress, Object fieldValue) throws GGAPIObjectQueryException;

	Object getValue(Object object, String fieldAddress) throws GGAPIObjectQueryException;
	Object getValue(Object object, GGAPIObjectAddress fieldAddress) throws GGAPIObjectQueryException;
	Object getValue(String fieldAddress) throws GGAPIObjectQueryException;
	Object getValue(GGAPIObjectAddress fieldAddress) throws GGAPIObjectQueryException;
	
	Object invoke(String methodAddress, Object ...args) throws GGAPIObjectQueryException;
	Object invoke(GGAPIObjectAddress methodAddress, Object ...args) throws GGAPIObjectQueryException;
	Object invoke(Object object, GGAPIObjectAddress methodAddress, Object ...args) throws GGAPIObjectQueryException;

}