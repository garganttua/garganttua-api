package com.garganttua.api.security.authentication.interfaces.spring.rest;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.garganttua.api.interfaces.spring.rest.GGAPIInterfaceSpringCustomizable;
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

		ResourceConfig.addResource(resourceConfigFile, GGAPISpringAuthenticationRestInterface.class);

	}

	private static void createReflectConfig(String path) throws IOException, NoSuchMethodException, SecurityException {
		File reflectConfigFile = NativeImageConfig.getReflectConfigFile(path);
		if (!reflectConfigFile.exists())
			reflectConfigFile.createNewFile();

		ReflectConfig reflectConfig = ReflectConfig.loadFromFile(reflectConfigFile);

		reflectConfig.addEntry(
				ReflectConfigEntryBuilder.builder(GGAPISpringAuthenticationRestInterface.class)
					.constructor(GGAPISpringAuthenticationRestInterface.class.getDeclaredConstructor())
					.method(GGAPISpringAuthenticationRestInterface.class.getMethod("authenticate", IGGAPICaller.class, GGAPISpringRestAuthenticationRequest.class))
					.build());


		reflectConfig.saveToFile(reflectConfigFile);
	}
}