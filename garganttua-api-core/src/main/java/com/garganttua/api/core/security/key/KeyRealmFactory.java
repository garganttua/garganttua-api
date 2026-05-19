package com.garganttua.api.core.security.key;

import java.security.KeyPair;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.definition.IDomainKeyDefinition;
import com.garganttua.core.crypto.IKeyAlgorithm;
import com.garganttua.core.crypto.KeyAlgorithm;
import com.garganttua.core.crypto.KeyType;
import com.garganttua.core.crypto.SignatureAlgorithm;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;

/**
 * Bridge between persisted {@code @Key} entities and the runtime
 * {@link MaterializedKeyRealm}. Two operations:
 *
 * <ul>
 *   <li>{@link #materialize(Object, IDomainKeyDefinition, IReflection)} —
 *       reads the entity fields described by an {@link IDomainKeyDefinition}
 *       and rebuilds an {@link com.garganttua.core.crypto.IKeyRealm}
 *       ready for sign / verify.</li>
 *   <li>{@link #generateAndStamp} — generates a fresh JDK
 *       {@link KeyPair}, instantiates the entity class and writes
 *       realmName / algorithm / signatureAlgorithm / publicMaterial /
 *       privateMaterial / expiration / revoked. Caller is responsible
 *       for {@code repository.save(...)}.</li>
 * </ul>
 *
 * <p>The factory deliberately knows nothing about how the entity is
 * scoped (tenantId, ownerId) — those are stamped by the higher-level
 * runtime caller that picks the scope from {@code AuthenticatorKeyUsage}.
 * Tenancy stamping is a concern of the resolve path, not the factory.
 */
public final class KeyRealmFactory {

	private KeyRealmFactory() {
	}

	/**
	 * Rebuilds an {@link com.garganttua.core.crypto.IKeyRealm} from a
	 * persisted key entity.
	 */
	public static MaterializedKeyRealm materialize(Object entity, IDomainKeyDefinition keyDef, IReflection reflection)
			throws ApiException {
		Objects.requireNonNull(entity, "entity");
		Objects.requireNonNull(keyDef, "keyDef");
		Objects.requireNonNull(reflection, "reflection");

		String realmName = readString(entity, keyDef.realmName(), reflection, "realmName");
		String algorithmRaw = readString(entity, keyDef.algorithm(), reflection, "algorithm");
		String signatureRaw = readString(entity, keyDef.signatureAlgorithm(), reflection, "signatureAlgorithm");
		byte[] publicBytes = readBytes(entity, keyDef.publicMaterial(), reflection, "publicMaterial");
		byte[] privateBytes = readBytes(entity, keyDef.privateMaterial(), reflection, "privateMaterial");
		Date expiration = readExpiration(entity, keyDef.expiration(), reflection);
		boolean revoked = readBoolean(entity, keyDef.revoked(), reflection);

		IKeyAlgorithm algorithm = parseAlgorithm(algorithmRaw);
		SignatureAlgorithm sigAlgo = parseSignature(signatureRaw);

		MaterializedKey privateKey = new MaterializedKey(KeyType.PRIVATE, algorithm, sigAlgo, privateBytes);
		MaterializedKey publicKey = new MaterializedKey(KeyType.PUBLIC, algorithm, sigAlgo, publicBytes);

		return new MaterializedKeyRealm(realmName, algorithm, expiration, revoked, privateKey, publicKey);
	}

	/**
	 * Generates a fresh JDK {@link KeyPair} matching {@code algorithm} /
	 * {@code signatureAlgorithm} and writes every field described by
	 * {@code keyDef} onto a new instance of {@code entityClass}.
	 *
	 * <p>The returned entity has no uuid / tenantId / ownerId stamped —
	 * the caller (the resolve path) is responsible for setting those
	 * based on {@code AuthenticatorKeyUsage} before persisting.
	 */
	public static Object generateAndStamp(IClass<?> entityClass, IDomainKeyDefinition keyDef,
			IKeyAlgorithm algorithm, SignatureAlgorithm signatureAlgorithm,
			String realmName, int duration, TimeUnit unit, IReflection reflection) throws ApiException {
		Objects.requireNonNull(entityClass, "entityClass");
		Objects.requireNonNull(keyDef, "keyDef");
		Objects.requireNonNull(algorithm, "algorithm");
		Objects.requireNonNull(signatureAlgorithm, "signatureAlgorithm");
		Objects.requireNonNull(realmName, "realmName");
		Objects.requireNonNull(reflection, "reflection");

		if (!(algorithm instanceof KeyAlgorithm concreteAlgo)) {
			throw new ApiException("KeyRealmFactory.generateAndStamp: algorithm must be a "
					+ KeyAlgorithm.class.getName() + " — got " + algorithm.getClass().getName());
		}

		KeyPair pair;
		try {
			pair = concreteAlgo.generateAsymmetricKey();
		} catch (Exception e) {
			throw new ApiException("KeyRealmFactory.generateAndStamp: keypair generation failed for "
					+ concreteAlgo + ": " + e.getMessage(), e);
		}

		Object entity;
		try {
			entity = entityClass.getConstructor().newInstance();
		} catch (Exception e) {
			throw new ApiException("KeyRealmFactory.generateAndStamp: cannot instantiate "
					+ entityClass.getName() + " — a no-arg constructor is required: " + e.getMessage(), e);
		}

		writeIfMapped(entity, keyDef.realmName(), realmName, reflection);
		// Store the algorithm in the canonical 'NAME-SIZE' form that
		// KeyAlgorithm.validateKeyAlgorithm consumes during materialize.
		// KeyAlgorithm.toString uses underscores ("EC_256"), which the
		// parser would reject — so we serialize explicitly.
		writeIfMapped(entity, keyDef.algorithm(),
				concreteAlgo.getName() + "-" + concreteAlgo.getKeySize(), reflection);
		writeIfMapped(entity, keyDef.signatureAlgorithm(), signatureAlgorithm.name(), reflection);
		writeIfMapped(entity, keyDef.publicMaterial(), pair.getPublic().getEncoded(), reflection);
		writeIfMapped(entity, keyDef.privateMaterial(), pair.getPrivate().getEncoded(), reflection);

		ObjectAddress expirationAddr = keyDef.expiration();
		if (expirationAddr != null) {
			Instant exp = Instant.now().plusMillis(unit == null || duration <= 0 ? 0L : unit.toMillis(duration));
			Object value = adaptExpiration(entityClass, expirationAddr, exp, reflection);
			reflection.setFieldValue(entity, expirationAddr, value);
		}

		writeIfMapped(entity, keyDef.revoked(), Boolean.FALSE, reflection);

		return entity;
	}

	// ───── readers ─────

	private static String readString(Object entity, ObjectAddress addr, IReflection reflection, String label)
			throws ApiException {
		if (addr == null) {
			throw new ApiException("KeyRealmFactory.materialize: '" + label
					+ "' field is not configured on the key entity definition");
		}
		Object value = reflection.getFieldValue(entity, addr.toString());
		if (value == null) {
			throw new ApiException("KeyRealmFactory.materialize: '" + label + "' field at " + addr + " is null");
		}
		return value.toString();
	}

	private static byte[] readBytes(Object entity, ObjectAddress addr, IReflection reflection, String label)
			throws ApiException {
		if (addr == null) {
			throw new ApiException("KeyRealmFactory.materialize: '" + label
					+ "' field is not configured on the key entity definition");
		}
		Object value = reflection.getFieldValue(entity, addr.toString());
		if (!(value instanceof byte[] bytes)) {
			throw new ApiException("KeyRealmFactory.materialize: '" + label + "' at " + addr
					+ " must be a byte[] — got " + (value == null ? "null" : value.getClass().getName()));
		}
		return bytes;
	}

	private static Date readExpiration(Object entity, ObjectAddress addr, IReflection reflection) throws ApiException {
		if (addr == null) {
			return null;
		}
		Object value = reflection.getFieldValue(entity, addr.toString());
		if (value == null) {
			return null;
		}
		if (value instanceof Date date) {
			return (Date) date.clone();
		}
		if (value instanceof Instant instant) {
			return Date.from(instant);
		}
		if (value instanceof Long millis) {
			return new Date(millis);
		}
		throw new ApiException("KeyRealmFactory.materialize: expiration at " + addr
				+ " must be Date / Instant / Long — got " + value.getClass().getName());
	}

	private static boolean readBoolean(Object entity, ObjectAddress addr, IReflection reflection) {
		if (addr == null) {
			return false;
		}
		Object value = reflection.getFieldValue(entity, addr.toString());
		return Boolean.TRUE.equals(value);
	}

	// ───── writers ─────

	private static void writeIfMapped(Object entity, ObjectAddress addr, Object value, IReflection reflection) {
		if (addr != null) {
			reflection.setFieldValue(entity, addr, value);
		}
	}

	/**
	 * Picks the right concrete value for the entity's expiration field type.
	 * The DSL accepts Date / Instant / Long — we adapt at write time by
	 * inspecting the declared field type via reflection so the user doesn't
	 * have to manage the conversion.
	 */
	private static Object adaptExpiration(IClass<?> entityClass, ObjectAddress addr, Instant exp, IReflection reflection)
			throws ApiException {
		var fieldOpt = reflection.findField(entityClass, addr.toString());
		if (fieldOpt.isEmpty()) return exp;
		java.lang.reflect.Type rawType = fieldOpt.get().getType().getType();
		if (!(rawType instanceof Class<?> targetType)) return exp;
		if (Instant.class.isAssignableFrom(targetType)) return exp;
		if (Date.class.isAssignableFrom(targetType)) return Date.from(exp);
		if (Long.class.isAssignableFrom(targetType) || targetType == long.class) return exp.toEpochMilli();
		throw new ApiException("KeyRealmFactory.generateAndStamp: cannot adapt expiration to "
				+ targetType.getName() + " — supported: Date, Instant, Long");
	}

	// ───── parsers ─────

	private static IKeyAlgorithm parseAlgorithm(String raw) throws ApiException {
		try {
			return KeyAlgorithm.validateKeyAlgorithm(raw);
		} catch (IllegalArgumentException e) {
			throw new ApiException("KeyRealmFactory.materialize: invalid algorithm '" + raw
					+ "' — expected format 'NAME-SIZE' (e.g. RSA-2048, EC-256): " + e.getMessage(), e);
		}
	}

	private static SignatureAlgorithm parseSignature(String raw) throws ApiException {
		try {
			return SignatureAlgorithm.valueOf(raw);
		} catch (IllegalArgumentException e) {
			throw new ApiException("KeyRealmFactory.materialize: invalid signatureAlgorithm '" + raw
					+ "' — must be a SignatureAlgorithm enum name (e.g. SHA256, SHA512): " + e.getMessage(), e);
		}
	}
}
