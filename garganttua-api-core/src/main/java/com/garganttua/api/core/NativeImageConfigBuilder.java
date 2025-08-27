package com.garganttua.api.core;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.garganttua.api.core.dto.GenericDto;
import com.garganttua.api.core.engine.Engine;
import com.garganttua.api.core.entity.GenericEntity;
import com.garganttua.api.core.entity.GenericTenantEntity;
import com.garganttua.api.core.security.authorization.Authorization;
import com.garganttua.api.core.security.authorization.SignableAuthorization;
import com.garganttua.api.core.security.engine.SecurityEngine;
import com.garganttua.api.core.security.key.KeyRealm;
import com.garganttua.api.core.service.Service;
import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.security.IAccessRule;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.nativve.image.config.NativeImageConfig;
import com.garganttua.nativve.image.config.reflection.ReflectConfig;
import com.garganttua.nativve.image.config.reflection.ReflectConfigEntryBuilder;
import com.garganttua.nativve.image.config.resources.ResourceConfig;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;
import com.garganttua.reflection.utils.IGGAnnotationScanner;

public class NativeImageConfigBuilder {

	static {
		GGObjectReflectionHelper.annotationScanner = new IGGAnnotationScanner() {

			@Override
			public List<Class<?>> getClassesWithAnnotation(String package_, Class<? extends Annotation> annotation) {
				Reflections reflections = new Reflections(package_, Scanners.TypesAnnotated);
				Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(annotation, true);
				return annotatedClasses.stream().collect(Collectors.toList());
			}
		};
	}

	public static void main(String[] args) throws IOException {
		createReflectConfig(args[0]);
		createResourceConfig(args[0]);
	}

	private static void createResourceConfig(String path) throws IOException {
		File resourceConfigFile = NativeImageConfig.getResourceConfigFile(path);
		if (!resourceConfigFile.exists())
			resourceConfigFile.createNewFile();
		
		ResourceConfig.addResource(resourceConfigFile, GenericDto.class);
		ResourceConfig.addResource(resourceConfigFile, GenericEntity.class);
		ResourceConfig.addResource(resourceConfigFile, GenericTenantEntity.class);
		ResourceConfig.addResource(resourceConfigFile, SignableAuthorization.class);
		ResourceConfig.addResource(resourceConfigFile, Authorization.class);
		ResourceConfig.addResource(resourceConfigFile, KeyRealm.class);

	}

	private static void createReflectConfig(String path) throws IOException {
		File reflectConfigFile = NativeImageConfig.getReflectConfigFile(path);
		if (!reflectConfigFile.exists())
			reflectConfigFile.createNewFile();

		ReflectConfig reflectConfig = ReflectConfig.loadFromFile(reflectConfigFile);
		
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(KeyRealm.class).queryAllDeclaredConstructors(true).allDeclaredFields(true).queryAllDeclaredMethods(true).build());
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(GenericDto.class).allDeclaredFields(true).queryAllDeclaredMethods(true).build());
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(SignableAuthorization.class).allDeclaredFields(true).queryAllDeclaredMethods(true).build());
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(Authorization.class).allDeclaredFields(true).queryAllDeclaredMethods(true).build());
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(Engine.class).allDeclaredFields(true).queryAllDeclaredMethods(true)
				.method("addAccessRule", IAccessRule.class)
				.method("addServicesInfos", IAccessRule.class, List.class)
				.method("addServicesInfos", IAccessRule.class, List.class)
				.method("close")
				.method("flush")
				.method("getAccessRules")
				.method("getAuthorities")
				.method("getAuthority", EntityOperation.class)
				.method("getCaller", String.class, EntityOperation.class, String.class, String.class, String.class, String.class, Object.class)
				.method("getDomain", String.class)
				.method("getDomains")
				.method("getFactory", String.class)
				.method("getService", String.class)
				.method("getServices")
				.method("getServicesInfos")
				.method("getTenantDomainName")
				.method("getTenantService")
				.method("getTenantsDomain")
				.method("init")
				.method("reload")
				.method("shutdown")
				.method("start")
				.method("stop")
				.build());
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(GenericEntity.class).allDeclaredFields(true).queryAllDeclaredMethods(true)
				.method("save", ICaller.class, Map.class)
				.method("delete", ICaller.class, Map.class)
				.build());
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(GenericTenantEntity.class).allDeclaredFields(true).queryAllDeclaredMethods(true)
				.method("save", ICaller.class, Map.class)
				.method("delete", ICaller.class, Map.class)
				.build());
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(SecurityEngine.class).allDeclaredFields(true).queryAllDeclaredMethods(true)
				.method("authenticate", IAuthenticationRequest.class)
				.method("authenticatorEntitySecurityPostProcessing", ICaller.class, Object.class, Map.class)
				.method("authenticatorEntitySecurityPreProcessing", ICaller.class, Object.class, Map.class)
				.method("close")
				.method("decodeAuthorizationFromRequest", Object.class, ICaller.class)
				.method("decodeRawAuthorization", byte[].class, ICaller.class)
				.method("flush")
				.method("getAuthenticationInterfacesRegistry")
				.method("init")
				.method("isStorableAuthorization", Object.class)
				.method("reload")
				.method("shutdown")
				.method("start")
				.method("stop")
				.method("verifyOwner", ICaller.class, Object.class)
				.method("verifyTenant", ICaller.class, Object.class)
				.build());
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(Service.class).allDeclaredFields(true).queryAllDeclaredMethods(true).build());
		reflectConfig.saveToFile(reflectConfigFile);
	}
}