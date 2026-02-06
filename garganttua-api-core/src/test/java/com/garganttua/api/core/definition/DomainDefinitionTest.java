package com.garganttua.api.core.definition;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.BusinessOperation;
import com.garganttua.api.spec.definition.IEntityDefinition;
import com.garganttua.api.spec.security.IAccessRule;

@DisplayName("DomainDefinition Tests")
class DomainDefinitionTest {

    // Test entity class
    public static class TestEntity {
        private String id;
        private String uuid;
        public String getId() { return id; }
        public String getUuid() { return uuid; }
    }

    private IEntityDefinition<TestEntity> mockEntityDefinition;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mockEntityDefinition = mock(IEntityDefinition.class);
        when(mockEntityDefinition.entityClass()).thenReturn(TestEntity.class);
    }

    private DomainDefinition<TestEntity> createDefinition(
            DomainSecurityDefinition securityDef,
            boolean creation, boolean readAll, boolean readOne,
            boolean update, boolean deleteOne, boolean deleteAll,
            boolean publik, boolean tenant) {
        return new DomainDefinition<>(
                "testentities",
                mockEntityDefinition,
                securityDef,
                List.of(),
                List.of(),
                creation, readAll, readOne, update, deleteAll, deleteOne,
                publik, tenant,
                List.of(), List.of(),
                null, null, null, null,
                Map.of(),
                Map.of()
        );
    }

    @Nested
    @DisplayName("accessRules() generation")
    class AccessRulesGeneration {

        @Test
        @DisplayName("still generates rules when security is disabled")
        void stillGeneratesRulesWhenSecurityDisabled() {
            DomainSecurityDefinition secDef = new DomainSecurityDefinition(
                    Access.anonymous, Access.anonymous, Access.anonymous,
                    Access.anonymous, Access.anonymous, Access.anonymous,
                    null, null, null, null, null, null,
                    true  // disabled - but rules are still generated
            );
            DomainDefinition<TestEntity> def = createDefinition(
                    secDef, true, true, true, true, true, true, false, false);

            List<IAccessRule> rules = def.accessRules();

            // Rules are generated even when security is disabled
            assertEquals(6, rules.size());
        }

        @Test
        @DisplayName("generates rules only for activated operations")
        void generatesRulesForActivatedOperations() {
            DomainSecurityDefinition secDef = new DomainSecurityDefinition(
                    Access.authenticated, Access.authenticated, Access.authenticated,
                    Access.authenticated, Access.authenticated, Access.authenticated,
                    false, false, false, false, false, false,
                    false
            );
            // Only creation and readAll activated
            DomainDefinition<TestEntity> def = createDefinition(
                    secDef, true, true, false, false, false, false, false, false);

            List<IAccessRule> rules = def.accessRules();

            assertEquals(2, rules.size());
            assertTrue(rules.stream().anyMatch(r -> r.operation().getBusinessOperation() == BusinessOperation.create));
            assertTrue(rules.stream().anyMatch(r -> r.operation().getBusinessOperation() == BusinessOperation.readAll));
        }

        @Test
        @DisplayName("generates all 6 rules when all operations activated")
        void generatesAllRulesWhenAllActivated() {
            DomainSecurityDefinition secDef = new DomainSecurityDefinition(
                    Access.authenticated, Access.authenticated, Access.authenticated,
                    Access.authenticated, Access.authenticated, Access.authenticated,
                    false, false, false, false, false, false,
                    false
            );
            DomainDefinition<TestEntity> def = createDefinition(
                    secDef, true, true, true, true, true, true, false, false);

            List<IAccessRule> rules = def.accessRules();

            assertEquals(6, rules.size());
        }
    }

    @Nested
    @DisplayName("Access level determination")
    class AccessLevelDetermination {

        @Test
        @DisplayName("uses securityDefinition access when provided")
        void usesSecurityDefinitionAccess() {
            DomainSecurityDefinition secDef = new DomainSecurityDefinition(
                    Access.owner,      // creation
                    Access.tenant,     // readAll
                    Access.anonymous,  // readOne
                    Access.authenticated, Access.authenticated, Access.authenticated,
                    false, false, false, false, false, false,
                    false
            );
            DomainDefinition<TestEntity> def = createDefinition(
                    secDef, true, true, true, false, false, false, false, false);

            List<IAccessRule> rules = def.accessRules();

            IAccessRule createRule = rules.stream()
                    .filter(r -> r.operation().getBusinessOperation() == BusinessOperation.create)
                    .findFirst().orElseThrow();
            IAccessRule readAllRule = rules.stream()
                    .filter(r -> r.operation().getBusinessOperation() == BusinessOperation.readAll)
                    .findFirst().orElseThrow();
            IAccessRule readOneRule = rules.stream()
                    .filter(r -> r.operation().getBusinessOperation() == BusinessOperation.readOne)
                    .findFirst().orElseThrow();

            assertEquals(Access.owner, createRule.access());
            assertEquals(Access.tenant, readAllRule.access());
            assertEquals(Access.anonymous, readOneRule.access());
        }

        @Test
        @DisplayName("falls back to anonymous when publik is true")
        void fallsBackToAnonymousWhenPublic() {
            DomainSecurityDefinition secDef = new DomainSecurityDefinition(
                    null, null, null, null, null, null,  // no specific access
                    false, false, false, false, false, false,
                    false
            );
            DomainDefinition<TestEntity> def = createDefinition(
                    secDef, true, false, false, false, false, false,
                    true,   // publik
                    false
            );

            List<IAccessRule> rules = def.accessRules();

            assertEquals(Access.anonymous, rules.get(0).access());
        }

        @Test
        @DisplayName("falls back to tenant when tenant is true")
        void fallsBackToTenantWhenTenantFlag() {
            DomainSecurityDefinition secDef = new DomainSecurityDefinition(
                    null, null, null, null, null, null,
                    false, false, false, false, false, false,
                    false
            );
            DomainDefinition<TestEntity> def = createDefinition(
                    secDef, true, false, false, false, false, false,
                    false,
                    true   // tenant
            );

            List<IAccessRule> rules = def.accessRules();

            assertEquals(Access.tenant, rules.get(0).access());
        }

        @Test
        @DisplayName("defaults to authenticated when no flags set")
        void defaultsToAuthenticated() {
            DomainSecurityDefinition secDef = new DomainSecurityDefinition(
                    null, null, null, null, null, null,
                    false, false, false, false, false, false,
                    false
            );
            DomainDefinition<TestEntity> def = createDefinition(
                    secDef, true, false, false, false, false, false,
                    false, false
            );

            List<IAccessRule> rules = def.accessRules();

            assertEquals(Access.authenticated, rules.get(0).access());
        }
    }

    @Nested
    @DisplayName("Authority generation")
    class AuthorityGeneration {

        @Test
        @DisplayName("generates authority when flag is true")
        void generatesAuthorityWhenFlagTrue() {
            DomainSecurityDefinition secDef = new DomainSecurityDefinition(
                    Access.authenticated, null, null, null, null, null,
                    false,
                    true,   // creationAuthority
                    false, false, false, false,
                    false
            );
            DomainDefinition<TestEntity> def = createDefinition(
                    secDef, true, false, false, false, false, false, false, false);

            List<IAccessRule> rules = def.accessRules();

            assertNotNull(rules.get(0).authority());
            assertTrue(rules.get(0).authority().contains("CREATE"));
        }

        @Test
        @DisplayName("authority is null when flag is false")
        void authorityNullWhenFlagFalse() {
            DomainSecurityDefinition secDef = new DomainSecurityDefinition(
                    Access.authenticated, null, null, null, null, null,
                    false,
                    false,  // creationAuthority = false
                    false, false, false, false,
                    false
            );
            DomainDefinition<TestEntity> def = createDefinition(
                    secDef, true, false, false, false, false, false, false, false);

            List<IAccessRule> rules = def.accessRules();

            assertNull(rules.get(0).authority());
        }

        @Test
        @DisplayName("each operation can have its own authority flag")
        void eachOperationHasOwnAuthorityFlag() {
            DomainSecurityDefinition secDef = new DomainSecurityDefinition(
                    Access.authenticated, Access.authenticated, Access.authenticated,
                    Access.authenticated, Access.authenticated, Access.authenticated,
                    false,  // deleteOneAuthority
                    true,   // creationAuthority
                    false,  // readAllAuthority
                    true,   // readOneAuthority
                    false, false,
                    false
            );
            DomainDefinition<TestEntity> def = createDefinition(
                    secDef, true, true, true, false, false, false, false, false);

            List<IAccessRule> rules = def.accessRules();

            IAccessRule createRule = rules.stream()
                    .filter(r -> r.operation().getBusinessOperation() == BusinessOperation.create)
                    .findFirst().orElseThrow();
            IAccessRule readAllRule = rules.stream()
                    .filter(r -> r.operation().getBusinessOperation() == BusinessOperation.readAll)
                    .findFirst().orElseThrow();
            IAccessRule readOneRule = rules.stream()
                    .filter(r -> r.operation().getBusinessOperation() == BusinessOperation.readOne)
                    .findFirst().orElseThrow();

            assertNotNull(createRule.authority());
            assertNull(readAllRule.authority());
            assertNotNull(readOneRule.authority());
        }
    }

    @Nested
    @DisplayName("Operation details")
    class OperationDetails {

        @Test
        @DisplayName("operations have correct domain name")
        void operationsHaveCorrectDomainName() {
            DomainSecurityDefinition secDef = new DomainSecurityDefinition(
                    Access.authenticated, null, null, null, null, null,
                    false, false, false, false, false, false,
                    false
            );
            DomainDefinition<TestEntity> def = createDefinition(
                    secDef, true, false, false, false, false, false, false, false);

            List<IAccessRule> rules = def.accessRules();

            assertEquals("testentities", rules.get(0).operation().domainName());
        }

        @Test
        @DisplayName("operations have correct entity class")
        void operationsHaveCorrectEntityClass() {
            DomainSecurityDefinition secDef = new DomainSecurityDefinition(
                    Access.authenticated, null, null, null, null, null,
                    false, false, false, false, false, false,
                    false
            );
            DomainDefinition<TestEntity> def = createDefinition(
                    secDef, true, false, false, false, false, false, false, false);

            List<IAccessRule> rules = def.accessRules();

            assertEquals(TestEntity.class, rules.get(0).operation().entity());
        }

        @Test
        @DisplayName("key() returns operation key")
        void keyReturnsOperationKey() {
            DomainSecurityDefinition secDef = new DomainSecurityDefinition(
                    Access.authenticated, null, null, null, null, null,
                    false, false, false, false, false, false,
                    false
            );
            DomainDefinition<TestEntity> def = createDefinition(
                    secDef, true, false, false, false, false, false, false, false);

            List<IAccessRule> rules = def.accessRules();

            assertNotNull(rules.get(0).key());
            assertTrue(rules.get(0).key().contains("testentities"));
        }
    }

    @Nested
    @DisplayName("Null securityDefinition handling")
    class NullSecurityDefinition {

        @Test
        @DisplayName("works with null securityDefinition using defaults")
        void worksWithNullSecurityDefinition() {
            DomainDefinition<TestEntity> def = createDefinition(
                    null,  // null security definition
                    true, true, false, false, false, false, false, false);

            List<IAccessRule> rules = def.accessRules();

            assertEquals(2, rules.size());
            // Should fall back to authenticated
            assertEquals(Access.authenticated, rules.get(0).access());
        }
    }
}
