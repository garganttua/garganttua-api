package com.garganttua.api.nativve.image.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.garganttua.api.commons.dto.annotations.Dto;
import com.garganttua.api.commons.entity.annotations.BusinessAnnotations.EntityAfterCreate;
import com.garganttua.api.commons.entity.annotations.BusinessAnnotations.EntityAfterDelete;
import com.garganttua.api.commons.entity.annotations.BusinessAnnotations.EntityAfterGet;
import com.garganttua.api.commons.entity.annotations.BusinessAnnotations.EntityAfterUpdate;
import com.garganttua.api.commons.entity.annotations.BusinessAnnotations.EntityBeforeCreate;
import com.garganttua.api.commons.entity.annotations.BusinessAnnotations.EntityBeforeDelete;
import com.garganttua.api.commons.entity.annotations.BusinessAnnotations.EntityBeforeUpdate;
import com.garganttua.api.commons.entity.annotations.Entity;
import com.garganttua.api.commons.entity.annotations.EntityAuthorizeUpdate;
import com.garganttua.api.commons.entity.annotations.EntityDeleteMethod;
import com.garganttua.api.commons.entity.annotations.EntityDeleteMethodProvider;
import com.garganttua.api.commons.entity.annotations.EntityEngine;
import com.garganttua.api.commons.entity.annotations.EntityGotFromRepository;
import com.garganttua.api.commons.entity.annotations.EntityHidden;
import com.garganttua.api.commons.entity.annotations.EntityId;
import com.garganttua.api.commons.entity.annotations.EntityLocation;
import com.garganttua.api.commons.entity.annotations.EntityMandatory;
import com.garganttua.api.commons.entity.annotations.EntityOwnerId;
import com.garganttua.api.commons.entity.annotations.EntityRepository;
import com.garganttua.api.commons.entity.annotations.EntitySaveMethod;
import com.garganttua.api.commons.entity.annotations.EntitySaveMethodProvider;
import com.garganttua.api.commons.entity.annotations.EntityShare;
import com.garganttua.api.commons.entity.annotations.EntitySuperOwner;
import com.garganttua.api.commons.entity.annotations.EntitySuperTenant;
import com.garganttua.api.commons.entity.annotations.EntityTenantId;
import com.garganttua.api.commons.entity.annotations.EntityUnicity;
import com.garganttua.api.commons.entity.annotations.EntityUuid;
import com.garganttua.api.commons.security.annotations.Authentication;
import com.garganttua.api.commons.security.annotations.Authenticator;
import com.garganttua.api.commons.security.annotations.Authorization;
import com.garganttua.api.commons.security.key.IKeyRealm;
import com.garganttua.nativve.image.config.NativeImageConfig;
import com.garganttua.nativve.image.config.reflection.IReflectConfigEntryBuilder;
import com.garganttua.nativve.image.config.reflection.ReflectConfig;
import com.garganttua.nativve.image.config.reflection.ReflectConfigEntry;
import com.garganttua.nativve.image.config.reflection.ReflectConfigEntryBuilder;
import com.garganttua.nativve.image.config.resources.ResourceConfig;
import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;
import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NativeImageConfigBuilder {

	public static void createConfiguration(String pathToConfiguration, List<String> packages) throws IOException {
		File reflectConfigFile = NativeImageConfig.getReflectConfigFile(pathToConfiguration);
		if (!reflectConfigFile.exists()) {
			log.info("Creation of reflection configuration in directory " + pathToConfiguration);
			reflectConfigFile.createNewFile();
		}

		File resourceConfigFile = NativeImageConfig.getResourceConfigFile(pathToConfiguration);
		if (!resourceConfigFile.exists()) {
			log.info("Creation of resources configuration in directory " + pathToConfiguration);
			resourceConfigFile.createNewFile();
		}
		
		ReflectConfig reflectConfig = ReflectConfig.loadFromFile(reflectConfigFile);

		List<Class<?>> entityClasses = new ArrayList<Class<?>>();
		List<Class<?>> dtoClasses = new ArrayList<Class<?>>();
		List<Class<?>> authenticatorClasses = new ArrayList<Class<?>>();
		List<Class<?>> authenticationClasses = new ArrayList<Class<?>>();
		List<Class<?>> authorizationClasses = new ArrayList<Class<?>>();
		List<Class<?>> ggBeanClasses = new ArrayList<Class<?>>();
		
		packages.forEach(p -> {
			log.atInfo().log("Scanning package {}", p);
			entityClasses.addAll(GGObjectReflectionHelper.getClassesWithAnnotation(p, Entity.class));
			dtoClasses.addAll(GGObjectReflectionHelper.getClassesWithAnnotation(p, Dto.class));
			ggBeanClasses.addAll(GGObjectReflectionHelper.getClassesWithAnnotation(p, GGBean.class));
			authenticatorClasses.addAll(GGObjectReflectionHelper.getClassesWithAnnotation(p, Authenticator.class));
			authenticationClasses.addAll(GGObjectReflectionHelper.getClassesWithAnnotation(p, Authentication.class));
			authorizationClasses.addAll(GGObjectReflectionHelper.getClassesWithAnnotation(p, Authorization.class));
		});

		processClasses(reflectConfig, resourceConfigFile, entityClasses, NativeImageConfigBuilder::processEntityClass);
		processClasses(reflectConfig, resourceConfigFile, dtoClasses, NativeImageConfigBuilder::processDtoClass);
		processClasses(reflectConfig, resourceConfigFile, authorizationClasses, NativeImageConfigBuilder::processAuthorizationClass);
		processClasses(reflectConfig, resourceConfigFile, ggBeanClasses, NativeImageConfigBuilder::processGGBeanClass);

		log.atInfo().log("Writing file in directory "+pathToConfiguration);
		reflectConfig.saveToFile(reflectConfigFile);
	}

	private static void processClasses(ReflectConfig reflectConfig, File resourceConfigFile,
			List<Class<?>> entityClasses, ClassProcessorInterface processor) {
		entityClasses.forEach(entityClass -> {
			try {
				ResourceConfig.addResource(resourceConfigFile, entityClass);
				reflectConfig.addEntry(processor.processClass(reflectConfig, entityClass));
			} catch (NoSuchMethodException | SecurityException e) {
				log.atWarn().log("Error", e);
			} catch (IOException e) {
				log.atWarn().log("Error", e);
			}
		});
	}
	
	private static ReflectConfigEntry processGGBeanClass(ReflectConfig reflectConfig, Class<?> entityClass) throws NoSuchMethodException, SecurityException  {
		log.atInfo().log("Processing bean "+entityClass.getSimpleName());
		IReflectConfigEntryBuilder entryBuilder = getReflectConfigEntryBuilder(reflectConfig, entityClass);
		entryBuilder.constructor(entityClass.getDeclaredConstructor());
		
		return entryBuilder.build();
	}
	
	private static ReflectConfigEntry processDtoClass(ReflectConfig reflectConfig, Class<?> entityClass) throws NoSuchMethodException, SecurityException  {
		log.atInfo().log("Processing dto "+entityClass.getSimpleName());
		IReflectConfigEntryBuilder entryBuilder = getReflectConfigEntryBuilder(reflectConfig, entityClass);
		entryBuilder.fieldsAnnotatedWith(GGFieldMappingRule.class).queryAllDeclaredMethods(true).queryAllDeclaredConstructors(true);
		
		return entryBuilder.build();
	}

	private static ReflectConfigEntry processAuthorizationClass(ReflectConfig reflectConfig, Class<?> entityClass) throws NoSuchMethodException, SecurityException  {
		log.atInfo().log("Processing authorization "+entityClass.getSimpleName());
		IReflectConfigEntryBuilder entryBuilder = getReflectConfigEntryBuilder(reflectConfig, entityClass);
		
		entryBuilder.constructor(entityClass.getConstructor(byte[].class, IKeyRealm.class));
		entryBuilder.constructor(entityClass.getConstructor(String.class, String.class, String.class, String.class, List.class, Date.class, Date.class, IKeyRealm.class));
		
		return entryBuilder.build();
	}


	private static ReflectConfigEntry processEntityClass(ReflectConfig reflectConfig, Class<?> entityClass) throws NoSuchMethodException, SecurityException {
		log.atInfo().log("Processing entity "+entityClass.getSimpleName());
		IReflectConfigEntryBuilder entryBuilder = getReflectConfigEntryBuilder(reflectConfig, entityClass);
		
		entryBuilder.constructor(entityClass.getConstructor());
		
		entryBuilder.methodsAnnotatedWith(EntityAfterGet.class);
		entryBuilder.methodsAnnotatedWith(EntityAfterCreate.class);
		entryBuilder.methodsAnnotatedWith(EntityAfterUpdate.class);
		entryBuilder.methodsAnnotatedWith(EntityAfterDelete.class);
		entryBuilder.methodsAnnotatedWith(EntityBeforeCreate.class);
		entryBuilder.methodsAnnotatedWith(EntityBeforeUpdate.class);
		entryBuilder.methodsAnnotatedWith(EntityBeforeDelete.class);
		
		entryBuilder.methodsAnnotatedWith(EntityDeleteMethod.class);
		entryBuilder.methodsAnnotatedWith(EntitySaveMethod.class);
		
		entryBuilder.allDeclaredFields(true);
		
		entryBuilder.fieldsAnnotatedWith(EntityUuid.class);
		entryBuilder.fieldsAnnotatedWith(EntityMandatory.class);
		entryBuilder.fieldsAnnotatedWith(EntityUnicity.class);
		entryBuilder.fieldsAnnotatedWith(EntityTenantId.class);
		entryBuilder.fieldsAnnotatedWith(EntityId.class);
		entryBuilder.fieldsAnnotatedWith(EntityAuthorizeUpdate.class);
		entryBuilder.fieldsAnnotatedWith(EntitySuperTenant.class);
		entryBuilder.fieldsAnnotatedWith(EntitySuperOwner.class);
		entryBuilder.fieldsAnnotatedWith(EntityOwnerId.class);
		entryBuilder.fieldsAnnotatedWith(EntityLocation.class);
		entryBuilder.fieldsAnnotatedWith(EntityHidden.class);
		entryBuilder.fieldsAnnotatedWith(EntityShare.class);
		entryBuilder.fieldsAnnotatedWith(EntityGotFromRepository.class);
		entryBuilder.fieldsAnnotatedWith(EntitySaveMethodProvider.class);
		entryBuilder.fieldsAnnotatedWith(EntityDeleteMethodProvider.class);
		entryBuilder.fieldsAnnotatedWith(EntityRepository.class);
		entryBuilder.fieldsAnnotatedWith(EntityEngine.class);
		
		return entryBuilder.build();
	}

	private static IReflectConfigEntryBuilder getReflectConfigEntryBuilder(ReflectConfig reflectConfig, Class<?> entityClass) {
		IReflectConfigEntryBuilder entryBuilder = null;
		Optional<ReflectConfigEntry> entry__ = reflectConfig.findEntryByName(entityClass);
		if( entry__.isPresent() ) {
			entryBuilder = ReflectConfigEntryBuilder.builder(entry__.get());
		} else {
			entryBuilder = ReflectConfigEntryBuilder.builder(entityClass);
		}
		return entryBuilder;
	}
	
}
