package com.garganttua.api.core;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.garganttua.reflection.utils.IGGAnnotationScanner;

public class ReflectionsAnnotationScanner implements IGGAnnotationScanner {

	@Override
	public List<Class<?>> getClassesWithAnnotation(String package_, Class<? extends Annotation> annotation) {
		Reflections reflections = new Reflections(package_, Scanners.TypesAnnotated);
		Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(annotation, true);
		return annotatedClasses.stream().collect(Collectors.toList());
	}

}
