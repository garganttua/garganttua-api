package com.garganttua.api.core.security.authorization.jwt;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.garganttua.api.core.security.authorization.GGAPISignableAuthorization;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwned;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationToByteArray;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationType;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@GGAPIEntityOwned
public class GGAPIJWTAuthorization extends GGAPISignableAuthorization {

	@GGAPIAuthorizationType
	private String type = "JWT";
	
	private String alg;

	public GGAPIJWTAuthorization() {
		super();
	}

	public GGAPIJWTAuthorization(byte[] raw) throws GGAPIException {
		super(raw);
	}

	public GGAPIJWTAuthorization(String uuid, String tenantId, String ownerUuid, List<String> authorities,
			Date creationDate, Date expirationDate) throws GGAPIException {
		super(uuid, uuid, tenantId, ownerUuid, authorities,
				creationDate, expirationDate);
	}

	@GGAPIAuthorizationToByteArray
	public byte[] toByteArray() throws GGAPIException {
		String encodedSignature = Base64.getEncoder().encodeToString(this.getSignature());

		this.alg = GGAPIJWTAlgorithms.from(this.keyAlgorithm, this.signatureAlgorithm).toString();

		String encodedAlg = Base64.getEncoder().encodeToString(this.algToJWTJsonString().getBytes());
		String encodedBody = Base64.getEncoder().encodeToString(this.toJWTJsonString().getBytes());
		String str = encodedAlg+"."+encodedBody+"."+encodedSignature;
		return str.getBytes();
	}

	protected byte[] getSignatureFromRaw(byte[] raw) {
		String[] chunks = new String(raw).split("\\.");
		Base64.Decoder decoder = Base64.getDecoder();
		return decoder.decode(chunks[2].getBytes());
	}

	@Override
	protected void decodeFromRaw(byte[] raw) throws GGAPISecurityException {
		try {

			String[] chunks = new String(raw).split("\\.");
			Base64.Decoder decoder = Base64.getDecoder();
			String payload = new String(decoder.decode(chunks[1]));

			DocumentContext jsonPayload = JsonPath.parse(payload);
			this.uuid = jsonPayload.read("$['jti']");
			this.tenantId = jsonPayload.read("$['tenantId']");
			this.authorities = jsonPayload.read("$['authorities'][*]");
			this.ownerId = jsonPayload.read("$['sub']");
			int creationDateInt = jsonPayload.read("$['iat']");
			int expirationDateInt = jsonPayload.read("$['exp']");
			this.creationDate = Date.from(Instant.ofEpochSecond(creationDateInt));
			this.expirationDate = Date.from(Instant.ofEpochSecond(expirationDateInt));

			String algo = new String(decoder.decode(chunks[0]));

			DocumentContext jsonAlgo = JsonPath.parse(algo);
			this.alg = jsonAlgo.read("$['alg']");

			GGAPIJWTAlgorithms aglo = GGAPIJWTAlgorithms.fromString(this.alg);
			this.keyAlgorithm = aglo.getKeyAlgorithm();
			this.signatureAlgorithm = aglo.getSignatureAlgorithm();

		} catch (Exception e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.BAD_REQUEST, "Unable to decrypt JWT token from raw");
		}
	}

	public String algToJWTJsonString() throws GGAPISecurityException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();

		node.put("alg", alg);

		return node.toString();
	}

	public String toJWTJsonString() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();

		node.put("sub", this.ownerId);
		node.put("jti", this.getUuid());
		node.put("tenantId", this.tenantId);
		node.put("iat", this.creationDate.getTime() / 1000);
		node.put("exp", this.expirationDate.getTime() / 1000);

		if (this.authorities != null) {
			node.putPOJO("authorities", this.authorities);
		}

		return node.toString();
	}

	@Override
	protected byte[] getDataToSign() {
		return this.toJWTJsonString().getBytes();
	}

	@Override
	public boolean isRevoked() {
		return this.revoked;
	}
}
