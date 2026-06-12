package com.garganttua.dao.mongodb;

import org.bson.Document;

import com.garganttua.api.commons.ApiException;
import com.garganttua.core.crypto.EncryptionMode;
import com.garganttua.core.crypto.EncryptionPaddingMode;
import com.garganttua.core.crypto.IKey;
import com.garganttua.core.crypto.IKeyAlgorithm;
import com.garganttua.core.crypto.KeyAlgorithm;
import com.garganttua.core.crypto.KeySerializer;
import com.garganttua.core.crypto.KeyType;
import com.garganttua.core.crypto.SignatureAlgorithm;

/**
 * Bridges {@link IKey} crypto key material to and from a self-describing BSON sub-document, so a
 * {@code MongoDao} can persist an {@code IKey}-typed DTO field. There is no BSON codec for
 * {@code IKey} (an interface wrapping a lazy {@code java.security.Key} behind a package-private
 * constructor), so a raw {@code IKey} value makes the driver fail with
 * {@code "Can't find a codec for class …crypto.Key"}.
 *
 * <p>This is <strong>purely a persistence</strong> concern — the DTO keeps its {@code IKey} field
 * type at runtime (the entity signs/verifies with it); only the stored shape changes. The material
 * round-trips through {@link KeySerializer#exportRawKey(IKey)} / {@link KeySerializer#importRawKey},
 * and the metadata needed to rebuild the key (type, algorithm, signature algorithm, encryption
 * mode/padding) is stored alongside.
 *
 * <p>Persisted shape:
 * <pre>{@code
 * { "__ikey": true, "type": "PRIVATE", "algorithm": "EC-256",
 *   "signatureAlgorithm": "SHA256", "rawKey": "<base64>" }
 * }</pre>
 *
 * <p><b>Limitation:</b> {@code IKey} does not expose its IV size, so encryption keys round-trip with
 * {@code ivSize = 0}. Signing keys (the key-store mint path) do not use it, so they are unaffected.
 */
final class IKeyBsonBridge {

	/** Marker key present on every IKey sub-document, so the read side recognises one. */
	static final String MARKER = "__ikey";

	private static final String TYPE = "type";
	private static final String ALGORITHM = "algorithm";
	private static final String SIGNATURE_ALGORITHM = "signatureAlgorithm";
	private static final String ENCRYPTION_MODE = "encryptionMode";
	private static final String ENCRYPTION_PADDING_MODE = "encryptionPaddingMode";
	private static final String IV_SIZE = "ivSize";
	private static final String RAW_KEY = "rawKey";

	private IKeyBsonBridge() {
	}

	/** True when {@code value} is a BSON document produced by {@link #toDocument(IKey)}. */
	static boolean isKeyDocument(Object value) {
		return value instanceof Document document && Boolean.TRUE.equals(document.get(MARKER));
	}

	/** Serialises an {@link IKey} to its persistable sub-document. */
	static Document toDocument(IKey key) {
		IKeyAlgorithm algorithm = key.getAlgorithm();
		Document document = new Document(MARKER, Boolean.TRUE);
		document.put(TYPE, key.getType().name());
		document.put(ALGORITHM, algorithm.getName() + "-" + algorithm.getKeySize());
		document.put(RAW_KEY, KeySerializer.exportRawKey(key));
		if (key.getSignatureAlgorithm() != null) {
			document.put(SIGNATURE_ALGORITHM, key.getSignatureAlgorithm().name());
		}
		if (key.getEncryptionMode() != null) {
			document.put(ENCRYPTION_MODE, key.getEncryptionMode().name());
		}
		if (key.getEncryptionPaddingMode() != null) {
			document.put(ENCRYPTION_PADDING_MODE, key.getEncryptionPaddingMode().name());
		}
		return document;
	}

	/** Reconstructs the {@link IKey} a {@link #toDocument(IKey)} document describes. */
	static IKey fromDocument(Document document) throws ApiException {
		try {
			KeyType type = KeyType.valueOf(document.getString(TYPE));
			IKeyAlgorithm algorithm = KeyAlgorithm.validateKeyAlgorithm(document.getString(ALGORITHM));
			SignatureAlgorithm signatureAlgorithm = document.containsKey(SIGNATURE_ALGORITHM)
					? SignatureAlgorithm.valueOf(document.getString(SIGNATURE_ALGORITHM))
					: null;
			EncryptionMode encryptionMode = document.containsKey(ENCRYPTION_MODE)
					? EncryptionMode.valueOf(document.getString(ENCRYPTION_MODE))
					: null;
			EncryptionPaddingMode encryptionPaddingMode = document.containsKey(ENCRYPTION_PADDING_MODE)
					? EncryptionPaddingMode.valueOf(document.getString(ENCRYPTION_PADDING_MODE))
					: null;
			int ivSize = document.get(IV_SIZE) instanceof Number number ? number.intValue() : 0;
			return KeySerializer.importRawKey(document.getString(RAW_KEY), type, algorithm, ivSize,
					encryptionMode, encryptionPaddingMode, signatureAlgorithm);
		} catch (RuntimeException e) {
			throw new ApiException("Failed to reconstruct an IKey from its persisted MongoDB form "
					+ "(algorithm=" + document.getString(ALGORITHM) + ", type=" + document.getString(TYPE) + ")", e);
		}
	}
}
