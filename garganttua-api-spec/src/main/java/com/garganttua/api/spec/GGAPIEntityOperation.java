package com.garganttua.api.spec;

import java.util.Objects;

import lombok.Getter;

public class GGAPIEntityOperation {
	
	public static GGAPIEntityOperation readOne(String domainName, Class<?> entity) {
		return new GGAPIEntityOperation(domainName, GGAPIMethod.read, entity, false, false);
	}
	public static GGAPIEntityOperation createOne(String domainName, Class<?> entity) {
		return new GGAPIEntityOperation(domainName, GGAPIMethod.create, entity, false, false);
	}
	public static GGAPIEntityOperation custom(String domainName, GGAPIMethod method, Class<?> entity, boolean actionOnAllEntities) {
		return new GGAPIEntityOperation(domainName, method, entity, actionOnAllEntities, true);
	}
	public static GGAPIEntityOperation deleteAll(String domainName, Class<?> entity) {
		return new GGAPIEntityOperation(domainName, GGAPIMethod.delete, entity, true, false);
	}
	public static GGAPIEntityOperation deleteOne(String domainName, Class<?> entity) {
		return new GGAPIEntityOperation(domainName, GGAPIMethod.delete, entity, false, false);
	}
	public static GGAPIEntityOperation updateOne(String domainName, Class<?> entity) {
		return new GGAPIEntityOperation(domainName, GGAPIMethod.update, entity, false, false);
	}
	public static GGAPIEntityOperation readAll(String domainName, Class<?> entity) {
		return new GGAPIEntityOperation(domainName, GGAPIMethod.read, entity, true, false);
	}
	public static GGAPIEntityOperation authenticate(String domainName, Class<?> request) {
		return new GGAPIEntityOperation(domainName, GGAPIMethod.authenticate, request, false, false);
	}

	@Getter
	private Class<?> entity;
	private String domainName;
	@Getter
	private GGAPIMethod method;
	@Getter
	private boolean actionOnAllEntities;
	@Getter
	private boolean custom = false;

	private GGAPIEntityOperation(String domainName, GGAPIMethod method, Class<?> entity, boolean actionOnAllEntities, boolean custom) {
		this.domainName = domainName;
		this.method = method;
		this.entity = entity;
		this.actionOnAllEntities = actionOnAllEntities;
		this.custom = custom;
	}

	@Override
	public String toString() {
		return this.domainName+"-"+method+"-"+(actionOnAllEntities?"all":"one")+"-"+(actionOnAllEntities?Pluralizer.toPlural(this.entity.getSimpleName().toLowerCase()):Singularizer.toSingular(this.entity.getSimpleName().toLowerCase()));
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj.toString().equals(this.toString());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(domainName, entity, method, actionOnAllEntities, custom);
	}
}
