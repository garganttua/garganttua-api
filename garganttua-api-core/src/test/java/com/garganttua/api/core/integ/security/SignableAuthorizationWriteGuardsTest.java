package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.expression.SecurityExpressions;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

/**
 * Guards on direct CRUD writes to a SIGNABLE authorization domain (decisions of
 * 2026-06-09):
 *
 *  <ul>
 *    <li>a direct (client) CRUD create is rejected with 403 — a signable token is
 *        only minted, already signed, by the authenticate/refresh pipeline;</li>
 *    <li>a framework-internal create (the authenticate-side persist, recognised by
 *        the server-set marker) is allowed;</li>
 *    <li>an update that flips a NON-signed field (revocation) is allowed;</li>
 *    <li>an update that changes a field covered by getDataToSign is rejected 400 —
 *        a signed authorization's signed material is immutable.</li>
 *  </ul>
 */
@DisplayName("Signable authorization — CRUD write guards")
class SignableAuthorizationWriteGuardsTest extends AbstractCrudScriptTest {

    /** getDataToSign covers uuid + tokenType (NOT revoked) — so revocation leaves the signature valid. */
    public static class Token {
        private String id;
        private String uuid;
        private String tenantId;
        private String ownerId;
        private String tokenType;
        private List<String> authorities;
        private Boolean revoked = false;
        private byte[] signature;
        private Boolean superTenant = false;

        public Token() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public List<String> getAuthorities() { return authorities; }
        public void setAuthorities(List<String> authorities) { this.authorities = authorities; }
        public Boolean getRevoked() { return revoked; }
        public void setRevoked(Boolean revoked) { this.revoked = revoked; }
        public byte[] getSignature() { return signature; }
        public void setSignature(byte[] signature) { this.signature = signature; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }

        public byte[] getDataToSign() {
            return (String.valueOf(uuid) + "|" + String.valueOf(tokenType)).getBytes(StandardCharsets.UTF_8);
        }
    }

    public static class TokenDto {
        private String id;
        private String uuid;
        private String tenantId;
        private String ownerId;
        private String tokenType;
        private List<String> authorities;
        private Boolean revoked;
        private byte[] signature;
        private Boolean superTenant;

        public TokenDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public List<String> getAuthorities() { return authorities; }
        public void setAuthorities(List<String> authorities) { this.authorities = authorities; }
        public Boolean getRevoked() { return revoked; }
        public void setRevoked(Boolean revoked) { this.revoked = revoked; }
        public byte[] getSignature() { return signature; }
        public void setSignature(byte[] signature) { this.signature = signature; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    private IDomain<?> tokenCtx;
    private CapturingDao tokenDao;

    @BeforeEach
    void setUp() throws ApiException {
        tokenDao = new CapturingDao();
        IApiBuilder builder = newBuilder();
        builder.domain(IClass.getClass(Token.class))
                .tenant(true)
                .superTenant("superTenant")
                .owned("ownerId")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .update("revoked").update("tokenType")
                .up()
                .dto(IClass.getClass(TokenDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(tokenDao)
                .up()
                .creation(true).readOne(true).readAll(true).update(true)
                .security()
                    .authorization()
                        .type("tokenType")
                        .authorities("authorities")
                        .revokable("revoked")
                        .signable()
                            .signature("signature")
                            .getDataToSign("getDataToSign")
                        .up()
                    .up()
                .up();

        IApi api = buildAndStart(builder);
        tokenCtx = api.getDomain("tokens").orElseThrow();
    }

    private Token token(String uuid, String tokenType, boolean revoked) {
        Token t = new Token();
        t.setUuid(uuid);
        t.setTenantId("SUPER_TENANT");
        t.setOwnerId("users:owner-1");
        t.setTokenType(tokenType);
        t.setRevoked(revoked);
        return t;
    }

    private TokenDto storedToken(String uuid, String tokenType, boolean revoked) {
        TokenDto dto = new TokenDto();
        dto.setId(uuid);
        dto.setUuid(uuid);
        dto.setTenantId("SUPER_TENANT");
        dto.setOwnerId("users:owner-1");
        dto.setTokenType(tokenType);
        dto.setRevoked(revoked);
        dto.setSignature("sig-bytes".getBytes(StandardCharsets.UTF_8));
        return dto;
    }

    private WorkflowResult create(OperationRequest request) {
        return executeScript(tokenCtx, request);
    }

    @Test
    @DisplayName("direct CRUD create of a signable authorization is rejected (403)")
    void directCreateRejected() {
        OperationDefinition op = OperationDefinition.createOne("tokens", IClass.getClass(Token.class), false, null, Access.anonymous);
        OperationRequest request = superTenantScriptRequest(op);
        request.arg("entity", token("tok-1", "access", false));

        WorkflowResult result = create(request);

        assertFalse(result.isSuccess(), "a client must not be able to create a signable authorization directly");
        assertEquals(403, result.code());
        assertTrue(tokenDao.getStorage().isEmpty(), "nothing must have been persisted");
    }

    @Test
    @DisplayName("framework-internal create (the authenticate-side persist) is allowed")
    void frameworkInternalCreateAllowed() {
        OperationDefinition op = OperationDefinition.createOne("tokens", IClass.getClass(Token.class), false, null, Access.anonymous);
        OperationRequest request = superTenantScriptRequest(op);
        request.arg("entity", token("tok-2", "access", false));
        // The marker invokeInternal sets for the authenticate/refresh persist.
        request.arg(SecurityExpressions.FRAMEWORK_INTERNAL_WRITE_ARG, Boolean.TRUE);

        WorkflowResult result = create(request);

        assertTrue(result.isSuccess(), () -> "framework-internal persist must pass the guard, got code " + result.code());
        assertEquals(1, tokenDao.getStorage().size(), "the internally-issued token must be persisted");
        assertEquals("tok-2", ((TokenDto) tokenDao.getStorage().get(0)).getUuid());
    }

    @Test
    @DisplayName("revocation (flipping a NON-signed field) is allowed")
    void revocationAllowed() {
        tokenDao.getStorage().add(storedToken("tok-3", "access", false));

        OperationDefinition op = OperationDefinition.updateOne("tokens", IClass.getClass(Token.class), false, null, Access.anonymous);
        OperationRequest request = superTenantScriptRequest(op);
        request.arg("type", "uuid");
        request.arg("identifier", "tok-3");
        // Same tokenType (signed material unchanged), only revoked flips to true.
        request.arg("entity", token("tok-3", "access", true));

        WorkflowResult result = executeScript(tokenCtx, request);

        assertTrue(result.isSuccess(), () -> "revocation must be allowed, got code " + result.code());
        TokenDto saved = (TokenDto) tokenDao.getLastSaved();
        assertNotNull(saved, "the revoked token must have been persisted");
        assertEquals(Boolean.TRUE, saved.getRevoked(), "the revoked flag must have been persisted");
        assertEquals("access", saved.getTokenType(), "the signed field must be untouched");
    }

    @Test
    @DisplayName("an update changing a signed field (getDataToSign) is rejected (400)")
    void signedFieldMutationRejected() {
        tokenDao.getStorage().add(storedToken("tok-4", "access", false));

        OperationDefinition op = OperationDefinition.updateOne("tokens", IClass.getClass(Token.class), false, null, Access.anonymous);
        OperationRequest request = superTenantScriptRequest(op);
        request.arg("type", "uuid");
        request.arg("identifier", "tok-4");
        // tokenType is covered by getDataToSign — changing it would invalidate the signature.
        request.arg("entity", token("tok-4", "refresh", false));

        WorkflowResult result = executeScript(tokenCtx, request);

        assertFalse(result.isSuccess(), "mutating signed material must be rejected");
        assertEquals(400, result.code());
        TokenDto stored = (TokenDto) tokenDao.getStorage().get(0);
        assertEquals("access", stored.getTokenType(), "the stored token must be unchanged after a rejected update");
    }
}
