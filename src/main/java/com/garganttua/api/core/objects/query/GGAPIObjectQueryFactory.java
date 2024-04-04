package com.garganttua.api.core.objects.query;

public class GGAPIObjectQueryFactory {
	
	public static IGGAPIObjectQuery objectQuery(Class<?> objectClass) throws GGAPIObjectQueryException {
		return new GGAPIObjectQuery(objectClass);
	}
	
	public static IGGAPIObjectQuery objectQuery(Object object) throws GGAPIObjectQueryException {
		return new GGAPIObjectQuery(object);
	}
	
	public static IGGAPIObjectQuery objectQuery(Class<?> objectClass, Object object) throws GGAPIObjectQueryException {
		return new GGAPIObjectQuery(objectClass, object);
	}
}
