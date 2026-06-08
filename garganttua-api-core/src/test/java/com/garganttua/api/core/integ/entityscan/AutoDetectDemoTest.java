package com.garganttua.api.core.integ.entityscan;

import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.core.api.Api;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.core.reflection.IClass;

class AutoDetectDemoTest extends AbstractCrudIntegrationTest {

    @Test
    void runDemo() throws ApiException, java.io.IOException {
        IApiBuilder builder = newBuilder();
        ((com.garganttua.api.core.api.ApiBuilder) builder)
                .withPackage("com.garganttua.api.core.integ.entityscan");
        ((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) builder).autoDetect(true);

        // Pre-register DAO for each domain (scanner does not wire DAOs yet).
        // The scanner re-enters these same domain/dto builders during build.
        builder.domain(IClass.getClass(EntityAnnotationScanTest.AutoTenant.class))
                .dto(IClass.getClass(EntityAnnotationScanTest.AutoTenantDto.class))
                    .db(new CapturingDao())
                .up().up();
        builder.domain(IClass.getClass(EntityAnnotationScanTest.AutoPublicHiddenable.class))
                .dto(IClass.getClass(EntityAnnotationScanTest.AutoPublicHiddenableDto.class))
                    .db(new CapturingDao())
                .up().up();
        // Ensure scanner ran first by triggering autoDetection explicitly is unnecessary
        // — the DependentBuilder runs doAutoDetection() during build().

        IApi api = buildAndStart(builder);

        StringBuilder out = new StringBuilder();
        out.append("\n========================================================\n");
        out.append("  Garganttua API — auto-detection summary\n");
        out.append("========================================================\n");
        ((Api) api).getSummaryItems().forEach((k, v) -> out.append("  ").append(k).append(" : ").append(v).append("\n"));
        out.append("\n  Detail per domain :\n");
        for (var entry : ((Api) api).getDomains().entrySet()) {
            IDomain<?> d = entry.getValue();
            var def = d.getDomainDefinition();
            out.append("    [").append(entry.getKey()).append("]\n");
            out.append("      entity         = ").append(def.entityDefinition().entityClass().getSimpleName()).append("\n");
            out.append("        id           = ").append(def.entityDefinition().id()).append("\n");
            out.append("        uuid         = ").append(def.entityDefinition().uuid()).append("\n");
            out.append("        tenantId     = ").append(def.entityDefinition().tenantId()).append("\n");
            out.append("      tenant?        = ").append(d.isTenantEntity()).append("\n");
            out.append("      public?        = ").append(d.isPublicEntity()).append("\n");
            out.append("      hiddenable     = ").append(def.hiddenable()).append("\n");
            out.append("      operations     = ").append(def.operations().stream()
                    .map(o -> o.getBusinessOperation() != null ? o.getBusinessOperation().getLabel() : o.technicalOperation().name())
                    .toList()).append("\n");
        }
        out.append("========================================================\n");
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/auto-detect-demo.txt"), out.toString());
    }
}
