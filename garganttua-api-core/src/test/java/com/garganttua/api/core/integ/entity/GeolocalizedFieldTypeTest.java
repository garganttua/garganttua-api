package com.garganttua.api.core.integ.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.core.reflection.IClass;

/**
 * The geolocalized field is type-checked: it must be an {@code org.geojson.Point}.
 * A field of any other type is rejected at declaration.
 */
@DisplayName(".geolocalized(field) forces the field type to org.geojson.Point")
class GeolocalizedFieldTypeTest extends AbstractCrudIntegrationTest {

    // ── Entity whose location field IS a Point (accepted) ──
    public static class GeoEntity {
        private String id;
        private String uuid;
        private String tenantId;
        private Boolean superTenant;
        private org.geojson.Point location;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
        public org.geojson.Point getLocation() { return location; }
        public void setLocation(org.geojson.Point location) { this.location = location; }
    }

    public static class GeoEntityDto {
        private String id;
        private String uuid;
        private String tenantId;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    }

    // ── Entity whose location field is a String (rejected) ──
    public static class BadGeoEntity {
        private String id;
        private String uuid;
        private String tenantId;
        private String location; // NOT a Point
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class BadGeoEntityDto {
        private String id;
        private String uuid;
        private String tenantId;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    }

    @Test
    @DisplayName("a Point location field is accepted; the domain is geolocalized")
    void pointLocationAccepted() throws Exception {
        IApiBuilder builder = newBuilder();
        builder.domain(IClass.getClass(GeoEntity.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .geolocalized("location")
                .dto(IClass.getClass(GeoEntityDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new CapturingDao())
                .up()
            .up();

        IApi api = buildAndStart(builder);
        IDomain<?> d = api.getDomain("geoentities").orElseThrow();
        assertTrue(d.isGeolocalized(), "the domain must report as geolocalized");
        assertNotNull(d.getDomainDefinition().geolocalized(),
                "the resolved location field address must be set");
    }

    @Test
    @DisplayName("a non-Point (String) location field is rejected at declaration")
    void nonPointLocationRejected() {
        IApiBuilder builder = newBuilder();
        Exception ex = assertThrows(Exception.class, () ->
                builder.domain(IClass.getClass(BadGeoEntity.class))
                        .tenant(true)
                        .entity()
                            .id("id").uuid("uuid").tenantId("tenantId")
                        .up()
                        .geolocalized("location")); // String field — must be refused
        assertTrue(ex.getMessage() != null
                        && (ex.getMessage().contains("location") || ex.getMessage().toLowerCase().contains("point")
                            || ex.getMessage().toLowerCase().contains("type")),
                "rejection must point at the field/type mismatch; got: " + ex.getMessage());
    }
}
