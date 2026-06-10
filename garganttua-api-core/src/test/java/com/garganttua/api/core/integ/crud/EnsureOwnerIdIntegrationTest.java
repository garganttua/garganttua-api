package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.expression.EntityLifecycleExpressions;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

/**
 * Covers {@code ensureOwnerId}: on an owned domain, creating an entity stamps
 * its ownerId field from the caller's (already-qualified) ownerId — unless the
 * caller is a super-owner, has no ownerId, or the field was pre-populated.
 */
@DisplayName("CreateOne ensureOwnerId Tests")
class EnsureOwnerIdIntegrationTest extends AbstractCrudScriptTest {

	public static class Doc {
		private String id;
		private String uuid;
		private String tenantId;
		private String ownerId;
		private String title;
		private Boolean superTenant = false;

		public Doc() {}
		public String getId() { return id; }
		public void setId(String id) { this.id = id; }
		public String getUuid() { return uuid; }
		public void setUuid(String uuid) { this.uuid = uuid; }
		public String getTenantId() { return tenantId; }
		public void setTenantId(String tenantId) { this.tenantId = tenantId; }
		public String getOwnerId() { return ownerId; }
		public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
		public String getTitle() { return title; }
		public void setTitle(String title) { this.title = title; }
		public Boolean getSuperTenant() { return superTenant; }
		public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
	}

	public static class DocDto {
		private String id;
		private String uuid;
		private String tenantId;
		private String ownerId;
		private String title;
		private Boolean superTenant;

		public DocDto() {}
		public String getId() { return id; }
		public void setId(String id) { this.id = id; }
		public String getUuid() { return uuid; }
		public void setUuid(String uuid) { this.uuid = uuid; }
		public String getTenantId() { return tenantId; }
		public void setTenantId(String tenantId) { this.tenantId = tenantId; }
		public String getOwnerId() { return ownerId; }
		public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
		public String getTitle() { return title; }
		public void setTitle(String title) { this.title = title; }
		public Boolean getSuperTenant() { return superTenant; }
		public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
	}

	private IApi context;
	private IDomain<?> docCtx;

	@BeforeEach
	void setUp() throws ApiException {
		IApiBuilder builder = newBuilder();
		builder.domain(IClass.getClass(Doc.class))
				.tenant(true)
				.superTenant("superTenant")
				.owned("ownerId")
				.entity()
					.id("id").uuid("uuid").tenantId("tenantId")
				.up()
				.dto(IClass.getClass(DocDto.class))
					.id("id").uuid("uuid").tenantId("tenantId")
					.db(new CapturingDao())
				.up()
				.security().disable(true).up()
			.up();

		context = buildAndStart(builder);
		docCtx = context.getDomain("docs").orElseThrow();
	}

	private OperationRequest createRequest() {
		OperationDefinition op = OperationDefinition.createOneWithStandardSecurity("docs", IClass.getClass(Doc.class));
		return tenantScriptRequest(op, "T1");
	}

	@Test
	@DisplayName("owned entity gets ownerId stamped from the caller's ownerId")
	void stampsOwnerIdFromCaller() throws ApiException {
		Doc doc = new Doc();
		doc.setTitle("spec");

		OperationRequest request = createRequest();
		request.arg(IOperationRequest.OWNER_ID, "users:owner-1");
		request.arg("entity", doc);

		WorkflowResult result = executeScript(docCtx, request);
		assertTrue(result.isSuccess(), "create should succeed");

		Doc out = (Doc) result.output();
		assertEquals("users:owner-1", out.getOwnerId(),
				"ownerId must be stamped from the caller's (qualified) ownerId");
	}

	@Test
	@DisplayName("ensureOwnerId is a no-op when the caller has no ownerId")
	void noOwnerIdIsNoOp() {
		// The full create pipeline on an owned domain requires the caller to carry
		// an ownerId (VERIFY_OWNER), so the no-ownerId skip branch is exercised
		// directly against the real domain context: a tenant caller with a null
		// ownerId (e.g. a super-owner, or an owner-less operation) must leave the
		// field untouched rather than stamping null garbage.
		Doc doc = new Doc();
		doc.setTitle("spec");
		ICaller callerWithoutOwner = Caller.createTenantCaller("T1"); // ownerId == null

		Object out = EntityLifecycleExpressions.ensureOwnerId(doc, callerWithoutOwner, docCtx);

		assertNull(((Doc) out).getOwnerId(), "a caller with no ownerId must leave the field null");
	}

	@Test
	@DisplayName("ensureOwnerId is a no-op on a non-owned domain")
	void nonOwnedDomainIsNoOp() throws ApiException {
		// Build a NON-owned domain and confirm ensureOwnerId never touches it even
		// when the caller carries an ownerId.
		IApiBuilder builder = newBuilder();
		builder.domain(IClass.getClass(Doc.class))
				.tenant(true)
				.superTenant("superTenant")
				.entity().id("id").uuid("uuid").tenantId("tenantId").up()
				.dto(IClass.getClass(DocDto.class)).id("id").uuid("uuid").tenantId("tenantId").db(new CapturingDao()).up()
			.up();
		IApi api = buildAndStart(builder);
		IDomain<?> plainCtx = api.getDomain("docs").orElseThrow();

		Doc doc = new Doc();
		ICaller owner = Caller.createTenantCallerWithOwnerId("T1", "users:owner-1");
		Object out = EntityLifecycleExpressions.ensureOwnerId(doc, owner, plainCtx);

		assertNull(((Doc) out).getOwnerId(), "non-owned domain → ownerId field must stay null");
	}

	@Test
	@DisplayName("a pre-populated ownerId is preserved, not overwritten by the caller")
	void preservesExplicitOwnerId() throws ApiException {
		Doc doc = new Doc();
		doc.setTitle("spec");
		doc.setOwnerId("users:explicit-owner");

		OperationRequest request = createRequest();
		request.arg(IOperationRequest.OWNER_ID, "users:owner-1");
		request.arg("entity", doc);

		WorkflowResult result = executeScript(docCtx, request);
		assertTrue(result.isSuccess(), "create should succeed");

		Doc out = (Doc) result.output();
		assertEquals("users:explicit-owner", out.getOwnerId(),
				"an explicitly-set ownerId must be preserved, not overwritten");
	}
}
