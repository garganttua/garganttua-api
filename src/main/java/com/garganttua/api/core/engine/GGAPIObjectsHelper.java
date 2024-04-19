package com.garganttua.api.core.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class GGAPIObjectsHelper {

	@Autowired
	protected ApplicationContext context;
	
	static public void isImplementingInterface(Class<?> clazz, Class<?> interfasse) throws GGAPIEngineException {
		if (!interfasse.isAssignableFrom(clazz)) {
			throw new GGAPIEngineException(
					"The class [" + clazz.getName() + "] must implements the "+interfasse.getName()+" interface.");
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getObjectFromConfiguration(String objectName, Class<T> superClass) throws GGAPIEngineException {
		T obj = null;

		String[] splits = objectName.split(":");
		Class<?> objClass;
		try {
			objClass = Class.forName(splits[1]);
		} catch (ClassNotFoundException e1) {
			throw new GGAPIEngineException(e1);
		}

		if (!superClass.isAssignableFrom(objClass)) {
			throw new GGAPIEngineException("The class [" + objClass.getName() + "] must implements the ["
					+ superClass.getCanonicalName() + "] interface.");
		}

		switch (splits[0]) {
		case "bean":
			obj = (T) this.context.getBean(objClass);
			break;
		case "class":
			try {
				Constructor<T> ctor = (Constructor<T>) objClass.getConstructor();
				obj = ctor.newInstance();
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new GGAPIEngineException(e);
			}
			break;
		default:
			throw new GGAPIEngineException("Invalid controller " + objectName + ", should be bean: or class:");
		}

		return obj;
	}
	
}
