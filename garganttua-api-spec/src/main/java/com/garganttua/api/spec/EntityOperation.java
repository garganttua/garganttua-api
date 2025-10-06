package com.garganttua.api.spec;

import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EntityOperation {

	public static EntityOperation readOne(String domainName, Class<?> entity) {
		return new EntityOperation(domainName, Method.read, entity, false, false);
	}

	public static EntityOperation createOne(String domainName, Class<?> entity) {
		return new EntityOperation(domainName, Method.create, entity, false, false);
	}

	public static EntityOperation custom(String domainName, Method method, Class<?> entity,
			boolean actionOnAllEntities) {
		return new EntityOperation(domainName, method, entity, actionOnAllEntities, true);
	}

	public static EntityOperation deleteAll(String domainName, Class<?> entity) {
		return new EntityOperation(domainName, Method.delete, entity, true, false);
	}

	public static EntityOperation deleteOne(String domainName, Class<?> entity) {
		return new EntityOperation(domainName, Method.delete, entity, false, false);
	}

	public static EntityOperation updateOne(String domainName, Class<?> entity) {
		return new EntityOperation(domainName, Method.update, entity, false, false);
	}

	public static EntityOperation readAll(String domainName, Class<?> entity) {
		return new EntityOperation(domainName, Method.read, entity, true, false);
	}

	public static EntityOperation authenticate(String domainName, Class<?> request) {
		return new EntityOperation(domainName, Method.authenticate, request, false, false);
	}

	@Getter
	private Class<?> entity;
	private String domainName;
	@Getter
	private Method method;
	@Getter
	private boolean actionOnAllEntities;
	@Getter
	private boolean custom = false;

	private EntityOperation(String domainName, Method method, Class<?> entity, boolean actionOnAllEntities,
			boolean custom) {
		this.domainName = domainName;
		this.method = method;
		this.entity = entity;
		this.actionOnAllEntities = actionOnAllEntities;
		this.custom = custom;
	}

	@Override
	public String toString() {
		return this.domainName + "-" + method + "-" + (actionOnAllEntities ? "all" : "one") + "-"
				+ (actionOnAllEntities ? Pluralizer.toPlural(this.entity.getSimpleName().toLowerCase())
						: Singularizer.toSingular(this.entity.getSimpleName().toLowerCase()));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		EntityOperation other = (EntityOperation) obj;
		return Objects.equals(domainName, other.domainName) &&
				Objects.equals(entity, other.entity) &&
				method == other.method &&
				actionOnAllEntities == other.actionOnAllEntities &&
				custom == other.custom;
	}

	@Override
	public int hashCode() {
		return Objects.hash(domainName, entity, method, actionOnAllEntities, custom);
	}
}
