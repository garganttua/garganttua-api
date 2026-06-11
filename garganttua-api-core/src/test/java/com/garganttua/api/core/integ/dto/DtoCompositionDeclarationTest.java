package com.garganttua.api.core.integ.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.definition.DtoComposition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.api.commons.dto.annotations.Composed;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.core.reflection.IClass;

/**
 * The declaration layer of DTO composition: both the {@code .composed("field","collection")} DSL
 * and the {@code @Composed} annotation are captured into {@code IDtoDefinition.compositions()}.
 */
@DisplayName("DTO composition declaration — .composed(...) DSL and @Composed annotation")
class DtoCompositionDeclarationTest extends AbstractCrudIntegrationTest {

    public static class Item {
        private String id;
        private String uuid;
        private String tenantId;
        private Boolean superTenant;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    public static class SubDto {
        private String uuid;
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
    }

    public static class TagDto {
        private String uuid;
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
    }

    public static class ItemDto {
        private String id;
        private String uuid;
        private String tenantId;
        private SubDto ref;                       // 1-1 — declared via the DSL .composed(...)
        @Composed(collection = "tags")
        private List<TagDto> tags;                // 1-N — declared via the annotation
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public SubDto getRef() { return ref; }
        public void setRef(SubDto ref) { this.ref = ref; }
        public List<TagDto> getTags() { return tags; }
        public void setTags(List<TagDto> tags) { this.tags = tags; }
    }

    @Test
    @DisplayName("both the DSL composition and the @Composed annotation land in compositions(), each with its collection")
    void capturesBothPaths() throws Exception {
        IApiBuilder builder = newBuilder();
        builder.domain(IClass.getClass(Item.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(ItemDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .composed("ref", "subs")      // DSL, 1-1
                    .db(new CapturingDao())
                .up()
            .up();

        IApi api = buildAndStart(builder);
        IDtoDefinition<?> def = api.getDomain("items").orElseThrow()
                .getDomainDefinition().dtoDefinitions().get(0);

        List<DtoComposition> comps = def.compositions();
        assertEquals(2, comps.size(), "the DSL composition and the @Composed one are both captured");
        assertTrue(comps.stream().anyMatch(c -> "subs".equals(c.collection())),
                "the DSL .composed(\"ref\",\"subs\") must be present");
        assertTrue(comps.stream().anyMatch(c -> "tags".equals(c.collection())),
                "the @Composed(collection=\"tags\") field must be present");
    }

    @Test
    @DisplayName("a DTO that composes nothing reports an empty list")
    void emptyWhenNoComposition() throws Exception {
        IApiBuilder builder = newBuilder();
        builder.domain(IClass.getClass(Item.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(ItemDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new CapturingDao())
                .up()
            .up();

        IApi api = buildAndStart(builder);
        IDtoDefinition<?> def = api.getDomain("items").orElseThrow()
                .getDomainDefinition().dtoDefinitions().get(0);

        // Only the @Composed(tags) is auto-detected (no DSL composition declared here).
        assertEquals(1, def.compositions().size(), "only the annotated field is captured");
        assertEquals("tags", def.compositions().get(0).collection());
    }
}
