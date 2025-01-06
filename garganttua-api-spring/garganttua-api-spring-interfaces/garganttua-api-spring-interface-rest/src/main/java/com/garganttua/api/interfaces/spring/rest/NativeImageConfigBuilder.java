package com.garganttua.api.interfaces.spring.rest;

import java.io.File;
import java.io.IOException;

import com.garganttua.nativve.image.config.NativeImageConfig;
import com.garganttua.nativve.image.config.reflection.ReflectConfig;
import com.garganttua.nativve.image.config.reflection.ReflectConfigEntryBuilder;
import com.garganttua.nativve.image.config.resources.ResourceConfig;

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
		
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(GGAPIInterfaceSpringRest.class).constructor(GGAPIInterfaceSpringRest.class.getDeclaredConstructor()).build());
		
		reflectConfig.saveToFile(reflectConfigFile);
	}
}