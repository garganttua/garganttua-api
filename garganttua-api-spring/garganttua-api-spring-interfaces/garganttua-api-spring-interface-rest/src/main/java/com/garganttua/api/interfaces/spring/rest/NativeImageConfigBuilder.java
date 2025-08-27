package com.garganttua.api.interfaces.spring.rest;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.garganttua.api.spec.caller.ICaller;
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

		ResourceConfig.addResource(resourceConfigFile, InterfaceSpringRest.class);

	}

	private static void createReflectConfig(String path) throws IOException, NoSuchMethodException, SecurityException {
		File reflectConfigFile = NativeImageConfig.getReflectConfigFile(path);
		if (!reflectConfigFile.exists())
			reflectConfigFile.createNewFile();

		ReflectConfig reflectConfig = ReflectConfig.loadFromFile(reflectConfigFile);

		reflectConfig.addEntry(
				ReflectConfigEntryBuilder.builder(InterfaceSpringRest.class)
					.field("requestMappingHandlerMapping")
					.constructor(InterfaceSpringRest.class.getDeclaredConstructor())
					.method(InterfaceSpringRest.class.getMethod("getEntities", ICaller.class, Map.class))
					.method(InterfaceSpringRest.class.getMethod("deleteAll", ICaller.class, Map.class))
					.method(InterfaceSpringRest.class.getMethod("createEntity", ICaller.class, String.class, Map.class))
					.method(InterfaceSpringRest.class.getMethod("getEntity", ICaller.class, String.class, Map.class))
					.method(InterfaceSpringRest.class.getMethod("updateEntity", ICaller.class, String.class, String.class, Map.class))
					.method(InterfaceSpringRest.class.getMethod("deleteEntity", ICaller.class, String.class, Map.class))
					.build());
	
		reflectConfig.addEntry(
				ReflectConfigEntryBuilder.builder(AbstractInterfaceSpringRest.class)
					.field("requestMappingHandlerMapping")
					.constructor(AbstractInterfaceSpringRest.class.getDeclaredConstructor())
					.method(AbstractInterfaceSpringRest.class.getMethod("getEntities", ICaller.class, Map.class))
					.method(AbstractInterfaceSpringRest.class.getMethod("deleteAll", ICaller.class, Map.class))
					.method(AbstractInterfaceSpringRest.class.getMethod("createEntity", ICaller.class, String.class, Map.class))
					.method(AbstractInterfaceSpringRest.class.getMethod("getEntity", ICaller.class, String.class, Map.class))
					.method(AbstractInterfaceSpringRest.class.getMethod("updateEntity", ICaller.class, String.class, String.class, Map.class))
					.method(AbstractInterfaceSpringRest.class.getMethod("deleteEntity", ICaller.class, String.class, Map.class))
					.build());
		
		reflectConfig.addEntry(
				ReflectConfigEntryBuilder.builder(InterfaceSpringCustomizable.class)
					.field("requestMappingHandlerMapping")
					.constructor(InterfaceSpringCustomizable.class.getDeclaredConstructor())
					.method(InterfaceSpringCustomizable.class.getMethod("customService", ICaller.class, Map.class, HttpServletRequest.class))
					.build());

		reflectConfig.saveToFile(reflectConfigFile);
	}
}