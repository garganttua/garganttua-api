package com.garganttua.api.core.objects.methods;

import java.util.List;

import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;

public class GGAPIObjectMethodInvoker {

	private Class<?> clazz;
	private List<Object> fields;
	private GGAPIObjectAddress address;

	public GGAPIObjectMethodInvoker(Class<?> clazz, List<Object> fields,
			GGAPIObjectAddress address) throws GGAPIObjectQueryException {
		if (clazz == null) {
			throw new GGAPIObjectQueryException("class is null");
		}
		if (fields == null) {
			throw new GGAPIObjectQueryException("fields is null");
		}
		if (address == null) {
			throw new GGAPIObjectQueryException("address is null");
		}

		this.clazz = clazz;
		this.fields = fields;
		this.address = address;
	}

	public Object invoke(Object ...args) {
		// TODO Auto-generated method stub
		return null;
	}

}
