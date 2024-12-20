package com.garganttua.api.nativve.image.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterCreate;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterDelete;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterGet;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterUpdate;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeCreate;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeDelete;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeUpdate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityDeleteMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityDeleteMethodProvider;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityEngine;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityGotFromRepository;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityHidden;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityLocation;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwnerId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityRepository;
import com.garganttua.api.spec.entity.annotations.GGAPIEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIEntitySaveMethodProvider;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityShare;
import com.garganttua.api.spec.entity.annotations.GGAPIEntitySuperOwner;
import com.garganttua.api.spec.entity.annotations.GGAPIEntitySuperTenant;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityTenantId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityUnicity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityUuid;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticator;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorization;
import com.garganttua.nativve.image.config.NativeImageConfig;
import com.garganttua.nativve.image.config.reflection.IReflectConfigEntryBuilder;
import com.garganttua.nativve.image.config.reflection.ReflectConfig;
import com.garganttua.nativve.image.config.reflection.ReflectConfigEntry;
import com.garganttua.nativve.image.config.reflection.ReflectConfigEntryBuilder;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NativeImageConfigBuilder {

	public static void createConfiguration(String pathToConfiguration, List<String> packages) throws IOException {
		log.atInfo().log("Creation of reflection configuration in directory "+pathToConfiguration);
		File reflectConfigFile = NativeImageConfig.getReflectConfigFile(pathToConfiguration);
		if( !reflectConfigFile.exists() ) reflectConfigFile.createNewFile();
		
		ReflectConfig reflectConfig = ReflectConfig.loadFromFile(reflectConfigFile);
		
		List<Class<?>> entityClasses = new ArrayList<Class<?>>();
		List<Class<?>> dtoClasses = new ArrayList<Class<?>>();
		List<Class<?>> authenticatorClasses = new ArrayList<Class<?>>();
		List<Class<?>> authenticationClasses = new ArrayList<Class<?>>();
		List<Class<?>> authorizationClasses = new ArrayList<Class<?>>();
		
		packages.forEach(p -> {
			log.atInfo().log("Scanning package {}", p);
			entityClasses.addAll(GGObjectReflectionHelper.getClassesWithAnnotation(p, GGAPIEntity.class));
			dtoClasses.addAll(GGObjectReflectionHelper.getClassesWithAnnotation(p, GGAPIDto.class));
			authenticatorClasses.addAll(GGObjectReflectionHelper.getClassesWithAnnotation(p, GGAPIAuthenticator.class));
			authenticationClasses.addAll(GGObjectReflectionHelper.getClassesWithAnnotation(p, GGAPIAuthentication.class));
			authorizationClasses.addAll(GGObjectReflectionHelper.getClassesWithAnnotation(p, GGAPIAuthorization.class));

		});

		processEntityClasses(reflectConfig, entityClasses);
		
		
		
		log.atInfo().log("Writing file in directory "+pathToConfiguration);
		reflectConfig.saveToFile(reflectConfigFile);
		
	}

	private static void processEntityClasses(ReflectConfig reflectConfig, List<Class<?>> entityClasses) {
		entityClasses.forEach(entityClass -> {
			try {
				reflectConfig.addEntry(processEntityClass(reflectConfig, entityClass));
			} catch (NoSuchMethodException | SecurityException e) {
				log.atWarn().log("Error", e);
			}
		});
	}

	private static ReflectConfigEntry processEntityClass(ReflectConfig reflectConfig, Class<?> entityClass) throws NoSuchMethodException, SecurityException {
		log.atInfo().log("Processing entity "+entityClass.getSimpleName());
		IReflectConfigEntryBuilder entryBuilder = getReflectConfigEntryBuilder(reflectConfig, entityClass);
		
		entryBuilder.constructor(entityClass.getDeclaredConstructor());
		
		entryBuilder.methodsAnnotatedWith(GGAPIEntityAfterGet.class);
		entryBuilder.methodsAnnotatedWith(GGAPIEntityAfterCreate.class);
		entryBuilder.methodsAnnotatedWith(GGAPIEntityAfterUpdate.class);
		entryBuilder.methodsAnnotatedWith(GGAPIEntityAfterDelete.class);
		entryBuilder.methodsAnnotatedWith(GGAPIEntityBeforeCreate.class);
		entryBuilder.methodsAnnotatedWith(GGAPIEntityBeforeUpdate.class);
		entryBuilder.methodsAnnotatedWith(GGAPIEntityBeforeDelete.class);
		
		entryBuilder.methodsAnnotatedWith(GGAPIEntityDeleteMethod.class);
		entryBuilder.methodsAnnotatedWith(GGAPIEntitySaveMethod.class);
		
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityUuid.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityMandatory.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityUnicity.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityTenantId.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityId.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityAuthorizeUpdate.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntitySuperTenant.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntitySuperOwner.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityOwnerId.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityLocation.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityHidden.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityShare.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityGotFromRepository.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntitySaveMethodProvider.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityDeleteMethodProvider.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityRepository.class);
		entryBuilder.fieldsAnnotatedWith(GGAPIEntityEngine.class);
		
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
