package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.api.Api;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.core.mapper.annotations.FieldMappingRule;
import com.garganttua.core.reflection.IClass;

/**
 * The configured super-tenant is super by definition, independent of whether the
 * persisted {@code superTenant} boolean survives the entity→DTO mapping. A DTO that
 * omits the field silently drops the stamp; before the fix the master was super on
 * the run that auto-created it (which registers it directly) yet lost its super
 * status on the next restart (auto-create skipped → only the persisted-field scan
 * runs). The registry must hold the configured id regardless.
 */
@DisplayName("Configured super-tenant stays super even when the DTO drops the field")
class SuperTenantAlwaysRegisteredTest extends AbstractCrudIntegrationTest {

	/** A tenant DTO that does NOT carry the superTenant field. */
	public static class NoStampDto {
		@FieldMappingRule(sourceFieldAddress = "id") private String id;
		@FieldMappingRule(sourceFieldAddress = "uuid") private String uuid;
		@FieldMappingRule(sourceFieldAddress = "tenantId") private String tenantId;
		@FieldMappingRule(sourceFieldAddress = "name") private String name;
		public String getId() { return id; } public void setId(String v) { id = v; }
		public String getUuid() { return uuid; } public void setUuid(String v) { uuid = v; }
		public String getTenantId() { return tenantId; } public void setTenantId(String v) { tenantId = v; }
		public String getName() { return name; } public void setName(String v) { name = v; }
	}

	private IApi buildTenantApi(CapturingDao dao) throws ApiException {
		IApiBuilder builder = newBuilder();
		builder.superTenantAutoCreate(true);
		builder.domain(IClass.getClass(User.class))
				.tenant(true).superTenant("superTenant")
				.entity().id("id").uuid("uuid").tenantId("tenantId").up()
				.dto(IClass.getClass(NoStampDto.class)).id("id").uuid("uuid").tenantId("tenantId").db(dao).up()
				.creation(true).readOne(true).readAll(true)
			.up();
		return buildAndStart(builder);
	}

	@Test
	@DisplayName("super on the auto-create run AND on a subsequent restart")
	void superTenantSurvivesRestart() throws ApiException {
		CapturingDao dao = new CapturingDao();

		// Run 1 — auto-creates the master and registers it directly.
		IApi run1 = buildTenantApi(dao);
		assertTrue(((Api) run1).isSuperTenant("SUPER_TENANT"),
				"the configured super-tenant must be registered on the auto-create run");

		// Run 2 — a fresh API over the SAME dao: the master already exists, so
		// auto-create is skipped and only scanSuperRegistries runs (reading the
		// persisted superTenant field, which the DTO dropped). It must STILL be super.
		IApi run2 = buildTenantApi(dao);
		assertTrue(((Api) run2).isSuperTenant("SUPER_TENANT"),
				"the configured super-tenant must stay super on restart, "
						+ "regardless of the persisted field surviving the DTO mapping");
		assertTrue(((Api) run2).getSuperTenantIds().contains("SUPER_TENANT"));
	}
}
