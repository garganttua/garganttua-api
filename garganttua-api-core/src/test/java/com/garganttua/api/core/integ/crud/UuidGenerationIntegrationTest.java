package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.IEntityBuilder;
import com.garganttua.api.commons.entity.IUuidGenerator;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

/**
 * Strong coverage for the createOne uuid-assignment policy:
 *
 *  <ul>
 *    <li>volet 1 — a missing client uuid is filled with a time-ordered UUID v7;</li>
 *    <li>default — a client-supplied uuid is preserved;</li>
 *    <li>volet 2 — {@code overwriteUuid(true)} discards the client uuid and regenerates;</li>
 *    <li>volet 3 — a domain {@code uuidGenerator(...)} drives the assigned value.</li>
 *  </ul>
 */
@DisplayName("UUID generation policy (createOne)")
class UuidGenerationIntegrationTest extends AbstractCrudScriptTest {

    @FunctionalInterface
    private interface EntityTweaks {
        void apply(IEntityBuilder<User> entity) throws ApiException;
    }

    @SuppressWarnings("unchecked")
    private IDomain<?> buildUserDomain(EntityTweaks entityTweaks) throws ApiException {
        IApiBuilder builder = newBuilder();
        IEntityBuilder<User> entity = (IEntityBuilder<User>) builder
                .domain(IClass.getClass(User.class))
                    .tenant(true)
                    .superTenant("superTenant")
                    .entity()
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .mandatory("name");
        entityTweaks.apply(entity);
        entity.up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new CapturingDao())
                .up()
                .security().disable(true).up()
            .up();

        IApi context = buildAndStart(builder);
        return context.getDomain("users").orElseThrow();
    }

    private User createAndGet(IDomain<?> domain, User input) throws ApiException {
        OperationDefinition createOp = OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(createOp);
        request.arg("entity", input);

        WorkflowResult result = executeScript(domain, request);
        assertTrue(result.isSuccess(), () -> "createOne should succeed, got code " + result.code());
        return (User) result.output();
    }

    // ───── default policy (overwriteUuid=false, no custom generator) ─────

    @Test
    @DisplayName("default: a missing uuid is filled with a time-ordered UUID v7")
    void missingUuidGetsV7() throws ApiException {
        IDomain<?> domain = buildUserDomain(e -> {});

        User input = new User();
        input.setName("Alice");

        User output = createAndGet(domain, input);

        assertNotNull(output.getUuid(), "uuid must have been generated");
        UUID parsed = assertDoesNotThrow(() -> UUID.fromString(output.getUuid()),
                "generated uuid must be a canonical UUID string");
        assertEquals(7, parsed.version(), "default generator must produce a v7 (time-ordered) uuid");
    }

    @Test
    @DisplayName("default: a client-supplied uuid is preserved untouched")
    void clientUuidPreserved() throws ApiException {
        IDomain<?> domain = buildUserDomain(e -> {});

        User input = new User();
        input.setUuid("client-provided-uuid");
        input.setName("Bob");

        User output = createAndGet(domain, input);

        assertEquals("client-provided-uuid", output.getUuid());
    }

    // ───── overwriteUuid(true) ─────

    @Test
    @DisplayName("overwriteUuid(true): discards a client-supplied uuid and regenerates a v7")
    void overwriteDiscardsClientUuid() throws ApiException {
        IDomain<?> domain = buildUserDomain(e -> e.overwriteUuid(true));

        User input = new User();
        input.setUuid("client-provided-uuid");
        input.setName("Charlie");

        User output = createAndGet(domain, input);

        assertNotEquals("client-provided-uuid", output.getUuid(),
                "overwriteUuid(true) must discard the client value");
        UUID parsed = assertDoesNotThrow(() -> UUID.fromString(output.getUuid()));
        assertEquals(7, parsed.version(), "regenerated uuid must still be a v7");
    }

    // ───── uuidGenerator(custom) ─────

    @Test
    @DisplayName("uuidGenerator: a missing uuid is filled by the custom generator (concrete value)")
    void customGeneratorFillsMissingUuid() throws ApiException {
        IUuidGenerator fixed = entity -> "FIXED-UUID";
        IDomain<?> domain = buildUserDomain(e -> e.uuidGenerator(fixed));

        User input = new User();
        input.setName("Diana");

        User output = createAndGet(domain, input);

        assertEquals("FIXED-UUID", output.getUuid());
    }

    @Test
    @DisplayName("uuidGenerator: the generator can derive the uuid from the entity's own fields")
    void customGeneratorSeesEntity() throws ApiException {
        IUuidGenerator fromName = entity -> "user:" + ((User) entity).getName();
        IDomain<?> domain = buildUserDomain(e -> e.uuidGenerator(fromName));

        User input = new User();
        input.setName("Eve");

        User output = createAndGet(domain, input);

        assertEquals("user:Eve", output.getUuid());
    }

    @Test
    @DisplayName("uuidGenerator: NOT used when the client supplies a uuid (overwrite off)")
    void customGeneratorSkippedWhenClientUuidPresent() throws ApiException {
        IUuidGenerator fixed = entity -> "FIXED-UUID";
        IDomain<?> domain = buildUserDomain(e -> e.uuidGenerator(fixed));

        User input = new User();
        input.setUuid("client-provided-uuid");
        input.setName("Frank");

        User output = createAndGet(domain, input);

        assertEquals("client-provided-uuid", output.getUuid(),
                "without overwriteUuid, a client uuid wins over the custom generator");
    }

    @Test
    @DisplayName("uuidGenerator + overwriteUuid(true): the generator overrides a client uuid")
    void customGeneratorWithOverwriteWins() throws ApiException {
        IUuidGenerator fixed = entity -> "FIXED-UUID";
        IDomain<?> domain = buildUserDomain(e -> e.overwriteUuid(true).uuidGenerator(fixed));

        User input = new User();
        input.setUuid("client-provided-uuid");
        input.setName("Grace");

        User output = createAndGet(domain, input);

        assertEquals("FIXED-UUID", output.getUuid(),
                "overwriteUuid(true) must let the custom generator override the client uuid");
    }
}
