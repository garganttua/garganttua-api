package com.garganttua.api.interfaces.spring.rest;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.nativve.image.config.NativeImageConfig;
import com.garganttua.nativve.image.config.reflection.ReflectConfig;
import com.garganttua.nativve.image.config.reflection.ReflectConfigEntryBuilder;
import com.garganttua.nativve.image.config.resources.ResourceConfig;

import jakarta.servlet.http.HttpServletRequest;

public class NativeImageConfigBuilder {

	public static void main(String[] args) throws IOException, NoSuchMethodException, SecurityException {
		createReflectConfig(args[0]);
		createResourceConfig(args[0]);
	}

	private static void createResourceConfig(String path) throws IOException {
		File resourceConfigFile = NativeImageConfig.getResourceConfigFile(path);
		if (!resourceConfigFile.exists())
			resourceConfigFile.createNewFile();

		ResourceConfig.addResource(resourceConfigFile, GGAPIInterfaceSpringRest.class);

	}

	private static void createReflectConfig(String path) throws IOException, NoSuchMethodException, SecurityException {
		File reflectConfigFile = NativeImageConfig.getReflectConfigFile(path);
		if (!reflectConfigFile.exists())
			reflectConfigFile.createNewFile();

		ReflectConfig reflectConfig = ReflectConfig.loadFromFile(reflectConfigFile);

		reflectConfig.addEntry(
				ReflectConfigEntryBuilder.builder(GGAPIInterfaceSpringRest.class)
					.field("requestMappingHandlerMapping")
					.constructor(GGAPIInterfaceSpringRest.class.getDeclaredConstructor())
					.method(GGAPIInterfaceSpringRest.class.getMethod("getEntities", IGGAPICaller.class, Map.class))
					.method(GGAPIInterfaceSpringRest.class.getMethod("deleteAll", IGGAPICaller.class, Map.class))
					.method(GGAPIInterfaceSpringRest.class.getMethod("createEntity", IGGAPICaller.class, String.class, Map.class))
					.method(GGAPIInterfaceSpringRest.class.getMethod("getEntity", IGGAPICaller.class, String.class, Map.class))
					.method(GGAPIInterfaceSpringRest.class.getMethod("updateEntity", IGGAPICaller.class, String.class, String.class, Map.class))
					.method(GGAPIInterfaceSpringRest.class.getMethod("deleteEntity", IGGAPICaller.class, String.class, Map.class))
					.build());
	
		reflectConfig.addEntry(
				ReflectConfigEntryBuilder.builder(GGAPIAbstractInterfaceSpringRest.class)
					.field("requestMappingHandlerMapping")
					.constructor(GGAPIAbstractInterfaceSpringRest.class.getDeclaredConstructor())
					.method(GGAPIAbstractInterfaceSpringRest.class.getMethod("getEntities", IGGAPICaller.class, Map.class))
					.method(GGAPIAbstractInterfaceSpringRest.class.getMethod("deleteAll", IGGAPICaller.class, Map.class))
					.method(GGAPIAbstractInterfaceSpringRest.class.getMethod("createEntity", IGGAPICaller.class, String.class, Map.class))
					.method(GGAPIAbstractInterfaceSpringRest.class.getMethod("getEntity", IGGAPICaller.class, String.class, Map.class))
					.method(GGAPIAbstractInterfaceSpringRest.class.getMethod("updateEntity", IGGAPICaller.class, String.class, String.class, Map.class))
					.method(GGAPIAbstractInterfaceSpringRest.class.getMethod("deleteEntity", IGGAPICaller.class, String.class, Map.class))
					.build());
		
		reflectConfig.addEntry(
				ReflectConfigEntryBuilder.builder(GGAPIInterfaceSpringCustomizable.class)
					.field("requestMappingHandlerMapping")
					.constructor(GGAPIInterfaceSpringCustomizable.class.getDeclaredConstructor())
					.method(GGAPIInterfaceSpringCustomizable.class.getMethod("customService", IGGAPICaller.class, Map.class, HttpServletRequest.class))
					.build());

		reflectConfig.saveToFile(reflectConfigFile);
	}
}