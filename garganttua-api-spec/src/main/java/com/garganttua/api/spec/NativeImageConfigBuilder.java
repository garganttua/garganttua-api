package com.garganttua.api.spec;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.garganttua.api.spec.security.IGGAPIPasswordEncoder;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationProtocol;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.nativve.image.config.NativeImageConfig;
import com.garganttua.nativve.image.config.reflection.ReflectConfig;
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
//		createReflectConfig();
		createResourceConfig(args[0]);
	}

	private static void createResourceConfig(String path) throws IOException {
		File resourceConfigFile = NativeImageConfig.getResourceConfigFile(path);
		if (!resourceConfigFile.exists())
			resourceConfigFile.createNewFile();
		
		ResourceConfig.addResource(resourceConfigFile, IGGAPIPasswordEncoder.class);
		ResourceConfig.addResource(resourceConfigFile, IGGAPIAuthorizationProtocol.class);
		ResourceConfig.addResource(resourceConfigFile, IGGAPIKeyRealm.class);

	}

	private static void createReflectConfig(String path) throws IOException {
		File reflectConfigFile = NativeImageConfig.getReflectConfigFile(path);
		if (!reflectConfigFile.exists())
			reflectConfigFile.createNewFile();

		ReflectConfig reflectConfig = ReflectConfig.loadFromFile(reflectConfigFile);
		
		
		reflectConfig.saveToFile(reflectConfigFile);
	}
}