package com.garganttua.api.spec;

import java.util.Objects;

import lombok.Getter;

public class GGAPIEntityOperation {
	
	public static GGAPIEntityOperation readOne(String domainName, String entityName) {
		return new GGAPIEntityOperation(domainName, GGAPIMethod.read, entityName, false, false);
	}
	public static GGAPIEntityOperation createOne(String domainName, String entityName) {
		return new GGAPIEntityOperation(domainName, GGAPIMethod.create, entityName, false, false);
	}
	public static GGAPIEntityOperation custom(String domainName, GGAPIMethod method, String entityName, boolean actionOnAllEntities) {
		return new GGAPIEntityOperation(domainName, method, entityName, actionOnAllEntities, true);
	}
	public static GGAPIEntityOperation deleteAll(String domainName, String entityName) {
		return new GGAPIEntityOperation(domainName, GGAPIMethod.delete, entityName, true, false);
	}
	public static GGAPIEntityOperation deleteOne(String domainName, String entityName) {
		return new GGAPIEntityOperation(domainName, GGAPIMethod.delete, entityName, false, false);
	}
	public static GGAPIEntityOperation updateOne(String domainName, String entityName) {
		return new GGAPIEntityOperation(domainName, GGAPIMethod.update, entityName, false, false);
	}
	public static GGAPIEntityOperation readAll(String domainName, String entityName) {
		return new GGAPIEntityOperation(domainName, GGAPIMethod.read, entityName, true, false);
	}

	private String entityName;
	private String domainName;
	@Getter
	private GGAPIMethod method;
	private boolean actionOnAllEntities;
	@Getter
	private boolean custom = false;

	private GGAPIEntityOperation(String domainName, GGAPIMethod method, String entityName, boolean actionOnAllEntities, boolean custom) {
		this.domainName = domainName;
		this.method = method;
		this.entityName = entityName;
		this.actionOnAllEntities = actionOnAllEntities;
		this.custom = custom;
	}

	@Override
	public String toString() {
		return this.domainName+"-"+method+"-"+(actionOnAllEntities?"all":"one")+"-"+(actionOnAllEntities?Pluralizer.toPlural(entityName):Singularizer.toSingular(entityName));
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj.toString().equals(this.toString());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(domainName, entityName, method, actionOnAllEntities, custom);
	}
}
