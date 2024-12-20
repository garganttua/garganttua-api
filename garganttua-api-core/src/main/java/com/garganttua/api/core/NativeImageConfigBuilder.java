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

import com.garganttua.api.core.dto.GenericGGAPIDto;
import com.garganttua.api.core.engine.GGApiEngine;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.core.entity.GenericGGAPITenantEntity;
import com.garganttua.api.core.security.authorization.GGAPIAuthorization;
import com.garganttua.api.core.security.authorization.GGAPISignableAuthorization;
import com.garganttua.api.core.security.engine.GGAPISecurityEngine;
import com.garganttua.api.core.security.key.GGAPIKeyRealm;
import com.garganttua.api.core.service.GGAPIService;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationRequest;
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
		
		ResourceConfig.addResource(resourceConfigFile, GenericGGAPIDto.class);
		ResourceConfig.addResource(resourceConfigFile, GenericGGAPIEntity.class);
		ResourceConfig.addResource(resourceConfigFile, GenericGGAPITenantEntity.class);
		ResourceConfig.addResource(resourceConfigFile, GGAPIAuthorization.class);
		ResourceConfig.addResource(resourceConfigFile, GGAPISignableAuthorization.class);
		ResourceConfig.addResource(resourceConfigFile, GGAPIKeyRealm.class);

	}

	private static void createReflectConfig(String path) throws IOException {
		File reflectConfigFile = NativeImageConfig.getReflectConfigFile(path);
		if (!reflectConfigFile.exists())
			reflectConfigFile.createNewFile();

		ReflectConfig reflectConfig = ReflectConfig.loadFromFile(reflectConfigFile);
		
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(GenericGGAPIDto.class).allDeclaredFields(true).queryAllDeclaredMethods(true).build());
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(GGApiEngine.class).allDeclaredFields(true).queryAllDeclaredMethods(true)
				.method("addAccessRule", IGGAPIAccessRule.class)
				.method("addServicesInfos", IGGAPIAccessRule.class, List.class)
				.method("addServicesInfos", IGGAPIAccessRule.class, List.class)
				.method("close")
				.method("flush")
				.method("getAccessRules")
				.method("getAuthorities")
				.method("getAuthority", GGAPIEntityOperation.class)
				.method("getCaller", String.class, GGAPIEntityOperation.class, String.class, String.class, String.class, String.class, Object.class)
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
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(GenericGGAPIEntity.class).allDeclaredFields(true).queryAllDeclaredMethods(true)
				.method("save", IGGAPICaller.class, Map.class)
				.method("delete", IGGAPICaller.class, Map.class)
				.build());
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(GenericGGAPITenantEntity.class).allDeclaredFields(true).queryAllDeclaredMethods(true)
				.method("save", IGGAPICaller.class, Map.class)
				.method("delete", IGGAPICaller.class, Map.class)
				.build());
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(GGAPISecurityEngine.class).allDeclaredFields(true).queryAllDeclaredMethods(true)
				.method("authenticate", IGGAPIAuthenticationRequest.class)
				.method("authenticatorEntitySecurityPostProcessing", IGGAPICaller.class, Object.class, Map.class)
				.method("authenticatorEntitySecurityPreProcessing", IGGAPICaller.class, Object.class, Map.class)
				.method("close")
				.method("decodeAuthorizationFromRequest", Object.class, IGGAPICaller.class)
				.method("decodeRawAuthorization", byte[].class, IGGAPICaller.class)
				.method("flush")
				.method("getAuthenticationInterfacesRegistry")
				.method("init")
				.method("isStorableAuthorization", Object.class)
				.method("reload")
				.method("shutdown")
				.method("start")
				.method("stop")
				.method("verifyOwner", IGGAPICaller.class, Object.class)
				.method("verifyTenant", IGGAPICaller.class, Object.class)
				.build());
		reflectConfig.addEntry(ReflectConfigEntryBuilder.builder(GGAPIService.class).allDeclaredFields(true).queryAllDeclaredMethods(true).build());
		reflectConfig.saveToFile(reflectConfigFile);
	}
}