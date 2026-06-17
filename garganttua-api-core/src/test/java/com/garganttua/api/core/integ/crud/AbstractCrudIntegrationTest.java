package com.garganttua.api.core.integ.crud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.garganttua.api.core.api.ApiBuilder;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.core.dsl.dependency.IDependentBuilder;
import com.garganttua.core.expression.dsl.ExpressionContextBuilder;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;
import com.garganttua.core.injection.context.dsl.InjectionContextBuilder;
import com.garganttua.core.mapper.annotations.FieldMappingRule;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;
import com.garganttua.core.runtime.RuntimeContextFactory;
import com.garganttua.core.runtime.dsl.IRuntimesBuilder;
import com.garganttua.core.runtime.dsl.RuntimesBuilder;
import com.garganttua.core.script.dsl.IScriptsBuilder;
import com.garganttua.core.script.dsl.ScriptsBuilder;
import com.garganttua.core.workflow.dsl.IWorkflowsBuilder;
import com.garganttua.core.workflow.dsl.WorkflowsBuilder;

public abstract class AbstractCrudIntegrationTest {

    // ───── Tenant entity: User ─────

    public static class User {
        private String id;
        private String uuid;
        private String tenantId;
        private String name;
        private String email;
        private Boolean enabled = true;
        private Boolean accountNonLocked = true;
        private Boolean accountNonExpired = true;
        private Boolean credentialsNonExpired = true;
        private Boolean superTenant = false;
        private Boolean superOwner = false;

        public User() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
        public Boolean getSuperOwner() { return superOwner; }
        public void setSuperOwner(Boolean superOwner) { this.superOwner = superOwner; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        public Boolean getAccountNonLocked() { return accountNonLocked; }
        public void setAccountNonLocked(Boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }
        public Boolean getAccountNonExpired() { return accountNonExpired; }
        public void setAccountNonExpired(Boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; }
        public Boolean getCredentialsNonExpired() { return credentialsNonExpired; }
        public void setCredentialsNonExpired(Boolean credentialsNonExpired) { this.credentialsNonExpired = credentialsNonExpired; }
    }

    public static class UserDto {
        @FieldMappingRule(sourceFieldAddress = "id")
        private String id;
        @FieldMappingRule(sourceFieldAddress = "uuid")
        private String uuid;
        @FieldMappingRule(sourceFieldAddress = "tenantId")
        private String tenantId;
        @FieldMappingRule(sourceFieldAddress = "name")
        private String name;
        @FieldMappingRule(sourceFieldAddress = "email")
        private String email;
        @FieldMappingRule(sourceFieldAddress = "superTenant")
        private Boolean superTenant;
        @FieldMappingRule(sourceFieldAddress = "superOwner")
        private Boolean superOwner;

        public UserDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
        public Boolean getSuperOwner() { return superOwner; }
        public void setSuperOwner(Boolean superOwner) { this.superOwner = superOwner; }
    }

    // ───── Non-tenant entity: Product ─────

    public static class Product {
        private String id;
        private String uuid;
        private String tenantId;
        private String label;
        private double price;
        private Boolean superTenant = false;

        public Product() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    public static class ProductDto {
        @FieldMappingRule(sourceFieldAddress = "id")
        private String id;
        @FieldMappingRule(sourceFieldAddress = "uuid")
        private String uuid;
        @FieldMappingRule(sourceFieldAddress = "tenantId")
        private String tenantId;
        @FieldMappingRule(sourceFieldAddress = "label")
        private String label;
        @FieldMappingRule(sourceFieldAddress = "price")
        private double price;
        @FieldMappingRule(sourceFieldAddress = "superTenant")
        private Boolean superTenant;

        public ProductDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    // ───── Stub DAO ─────

    public static class StubDao implements IDao {
        private final List<Object> storage = new ArrayList<>();
        private IClass<?> dtoClass;

        @Override
        public void registerDomain(IDomainDefinition domainDefinition) {
        }

        @Override
        public List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort)
                throws ApiException {
            List<Object> result = new ArrayList<>(storage);
            // Honor the pageable (slicing) so pagination is exercised end-to-end, like a real DAO.
            if (pageable.isPresent()) {
                IPageable p = pageable.get();
                int from = Math.min(Math.max(p.getPageIndex(), 0) * p.getPageSize(), result.size());
                int to = Math.min(from + p.getPageSize(), result.size());
                result = new ArrayList<>(result.subList(from, to));
            }
            return result;
        }

        @Override
        public Object save(Object object) throws ApiException {
            storage.add(object);
            return object;
        }

        @Override
        public void delete(Object object) throws ApiException {
            String deleteUuid = extractUuid(object);
            if (deleteUuid != null) {
                storage.removeIf(stored -> deleteUuid.equals(extractUuid(stored)));
            } else {
                storage.remove(object);
            }
        }

        private String extractUuid(Object obj) {
            try {
                java.lang.reflect.Field field = obj.getClass().getDeclaredField("uuid");
                field.setAccessible(true);
                Object value = field.get(obj);
                return value != null ? value.toString() : null;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public long count(IFilter filter) throws ApiException {
            return storage.size();
        }

        public List<Object> getStorage() {
            return storage;
        }

        public IClass<?> getDtoClass() {
            return dtoClass;
        }
    }

    // ───── Failing DAO ─────

    public static class FailingDao implements IDao {
        private IClass<?> dtoClass;

        @Override
        public void registerDomain(IDomainDefinition domainDefinition) {
        }

        @Override
        public List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort)
                throws ApiException {
            throw new ApiException("Database connection lost");
        }

        @Override
        public Object save(Object object) throws ApiException {
            throw new ApiException("Database connection lost");
        }

        @Override
        public void delete(Object object) throws ApiException {
            throw new ApiException("Database connection lost");
        }

        @Override
        public long count(IFilter filter) throws ApiException {
            throw new ApiException("Database connection lost");
        }
    }

    // ───── Capturing DAO ─────

    public static class CapturingDao implements IDao {
        private final List<Object> storage = new ArrayList<>();
        private IClass<?> dtoClass;
        private Optional<IPageable> lastPageable;
        private Optional<IFilter> lastFilter;
        private Optional<ISort> lastSort;
        private Object lastSaved;
        private Object lastDeleted;

        @Override
        public void registerDomain(IDomainDefinition domainDefinition) {}

        @Override
        public List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort)
                throws ApiException {
            this.lastPageable = pageable;
            this.lastFilter = filter;
            this.lastSort = sort;
            List<Object> result = filter.isPresent() ? filterStorage(filter.get()) : new ArrayList<>(storage);
            // Honor the pageable so pagination (slicing) is exercised end-to-end, like a real DAO:
            // skip pageIndex*pageSize, take pageSize. count() still reports the unpaginated total.
            if (pageable.isPresent()) {
                IPageable p = pageable.get();
                int from = Math.min(Math.max(p.getPageIndex(), 0) * p.getPageSize(), result.size());
                int to = Math.min(from + p.getPageSize(), result.size());
                result = new ArrayList<>(result.subList(from, to));
            }
            return result;
        }

        private List<Object> filterStorage(com.garganttua.api.commons.filter.IFilter f) {
            if ("$and".equals(f.getName()) && f.getFilters() != null) {
                List<Object> result = new ArrayList<>(storage);
                for (com.garganttua.api.commons.filter.IFilter sub : f.getFilters()) {
                    result = filterList(result, sub);
                }
                return result;
            }
            return filterList(new ArrayList<>(storage), f);
        }

        private List<Object> filterList(List<Object> list, com.garganttua.api.commons.filter.IFilter f) {
            if ("$field".equals(f.getName()) && f.getFilters() != null) {
                String fieldName = String.valueOf(f.getValue());
                com.garganttua.api.commons.filter.IFilter operator = f.getFilters().get(0);
                if ("$eq".equals(operator.getName())) {
                    Object expected = operator.getValue();
                    List<Object> result = new ArrayList<>();
                    for (Object obj : list) {
                        try {
                            java.lang.reflect.Field field = readField(obj, fieldName);
                            Object actual = field.get(obj);
                            if (expected != null && expected.equals(actual)) {
                                result.add(obj);
                            }
                        } catch (Exception e) { /* skip */ }
                    }
                    return result;
                }
                if ("$ne".equals(operator.getName())) {
                    // Keep rows whose field differs from the value — models the unicity self-exclusion
                    // ($ne uuid) so an UPDATE does not see the row it is updating as a duplicate.
                    Object expected = operator.getValue();
                    List<Object> result = new ArrayList<>();
                    for (Object obj : list) {
                        try {
                            java.lang.reflect.Field field = readField(obj, fieldName);
                            Object actual = field.get(obj);
                            if (expected == null ? actual != null : !expected.equals(actual)) {
                                result.add(obj);
                            }
                        } catch (Exception e) {
                            result.add(obj); // field unreadable → not equal → keep
                        }
                    }
                    return result;
                }
                if ("$gt".equals(operator.getName())) {
                    Object threshold = operator.getValue();
                    if (threshold == null) return list;
                    List<Object> result = new ArrayList<>();
                    for (Object obj : list) {
                        try {
                            java.lang.reflect.Field field = readField(obj, fieldName);
                            Object actual = field.get(obj);
                            if (actual instanceof Comparable<?> && compareSafely(actual, threshold) > 0) {
                                result.add(obj);
                            }
                        } catch (Exception e) { /* skip */ }
                    }
                    return result;
                }
            }
            return list;
        }

        private static java.lang.reflect.Field readField(Object obj, String fieldName) throws NoSuchFieldException {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private static int compareSafely(Object actual, Object threshold) {
            // Comparable contract: both operands must be of the same (or compatible)
            // type. The framework filters built by lookupValidAuthorization use the
            // same Instant type as the stored entity field, so this is safe here.
            return ((Comparable) actual).compareTo(threshold);
        }

        @Override
        public Object save(Object object) throws ApiException {
            this.lastSaved = object;
            storage.add(object);
            return object;
        }

        @Override
        public void delete(Object object) throws ApiException {
            this.lastDeleted = object;
            String deleteUuid = extractUuid(object);
            if (deleteUuid != null) {
                storage.removeIf(stored -> deleteUuid.equals(extractUuid(stored)));
            } else {
                storage.remove(object);
            }
        }

        private String extractUuid(Object obj) {
            try {
                java.lang.reflect.Field field = obj.getClass().getDeclaredField("uuid");
                field.setAccessible(true);
                Object value = field.get(obj);
                return value != null ? value.toString() : null;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public long count(IFilter filter) throws ApiException {
            return storage.size();
        }

        public List<Object> getStorage() { return storage; }
        public Optional<IPageable> getLastPageable() { return lastPageable; }
        public Optional<IFilter> getLastFilter() { return lastFilter; }
        public Optional<ISort> getLastSort() { return lastSort; }
        public Object getLastSaved() { return lastSaved; }
        public Object getLastDeleted() { return lastDeleted; }
    }

    // ───── Helper methods ─────

    protected static IApiBuilder newBaseBuilder() throws ApiException {
        com.garganttua.core.reflection.dsl.IReflectionBuilder reflectionBuilder = ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .withScanner(new ReflectionsAnnotationScanner());
        IClass.setReflection(reflectionBuilder.build());

        IApiBuilder builder = ApiBuilder.builder();

        IInjectionContextBuilder injectionContextBuilder = InjectionContextBuilder.builder()
                .childContextFactory(new RuntimeContextFactory());

        // InjectionContextBuilder requires IReflectionBuilder; provide before building
        ((IDependentBuilder<IInjectionContextBuilder, ?>) injectionContextBuilder).provide(reflectionBuilder);
        injectionContextBuilder.build();

        // ExpressionContextBuilder uses IInjectionContextBuilder; provide before building
        IExpressionContextBuilder expressionContextBuilder = ExpressionContextBuilder.builder();
        expressionContextBuilder.autoDetect(true);
        expressionContextBuilder.withPackage("com.garganttua.core.expression.functions");
        expressionContextBuilder.withPackage("com.garganttua.core.script.functions");
        // Required for script-side observe("start"|"end", source) markers
        // emitted by ScriptGenerator when workflowTiming is enabled — those
        // calls resolve against ObservabilityExpressions in this package.
        expressionContextBuilder.withPackage("com.garganttua.core.observability");
        expressionContextBuilder.withPackage("com.garganttua.api.core.expression");
        ((IDependentBuilder<IExpressionContextBuilder, ?>) expressionContextBuilder).provide(injectionContextBuilder);
        expressionContextBuilder.build();

        ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(reflectionBuilder);
        ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(injectionContextBuilder);
        ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(expressionContextBuilder);

        // ApiBuilder now requires an IWorkflowsBuilder (core 2.0.0-ALPHA02
        // dropped the public WorkflowBuilder.create() factory). Stand up the
        // chain Runtimes → Scripts → Workflows and provide() the leaf.
        IRuntimesBuilder runtimesBuilder = RuntimesBuilder.builder();
        ((IDependentBuilder<IRuntimesBuilder, ?>) runtimesBuilder).provide(injectionContextBuilder);
        IScriptsBuilder scriptsBuilder = ScriptsBuilder.builder();
        ((IDependentBuilder<IScriptsBuilder, ?>) scriptsBuilder).provide(injectionContextBuilder);
        ((IDependentBuilder<IScriptsBuilder, ?>) scriptsBuilder).provide(expressionContextBuilder);
        ((IDependentBuilder<IScriptsBuilder, ?>) scriptsBuilder).provide(runtimesBuilder);
        IWorkflowsBuilder workflowsBuilder = WorkflowsBuilder.builder();
        ((IDependentBuilder<IWorkflowsBuilder, ?>) workflowsBuilder).provide(injectionContextBuilder);
        ((IDependentBuilder<IWorkflowsBuilder, ?>) workflowsBuilder).provide(scriptsBuilder);
        ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(workflowsBuilder);

        return builder;
    }

    protected static IApiBuilder newBuilder() throws ApiException {
        IApiBuilder builder = newBaseBuilder();
        builder.superTenantId("SUPER_TENANT")
               .superTenantAutoCreate(false);
        return builder;
    }

    protected static IApi buildAndStart(IApiBuilder builder) throws ApiException {
        IApi context = builder.build();
        context.onInit();
        context.onStart();
        return context;
    }

    protected static OperationRequest superTenantRequest(OperationDefinition operation) {
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, operation);
        request.arg(IOperationRequest.TENANT_ID, "SUPER_TENANT");
        request.arg(IOperationRequest.REQUESTED_TENANT_ID, "SUPER_TENANT");
        request.arg(IOperationRequest.SUPER_TENANT, true);
        request.arg(IOperationRequest.SUPER_OWNER, true);
        return request;
    }

    /**
     * Non-super tenant caller. Use this when the test wants to exercise the
     * full security pipeline (token decoding, rejection paths). Since the
     * 2026-05-18 security-flaw remediation removed the super-tenant bypass
     * from VERIFY_AUTHORIZATION, super-callers and tenant callers traverse
     * the same path — but this helper keeps the intent explicit.
     */
    protected static OperationRequest tenantRequest(OperationDefinition operation, String tenantId) {
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, operation);
        request.arg(IOperationRequest.TENANT_ID, tenantId);
        request.arg(IOperationRequest.REQUESTED_TENANT_ID, tenantId);
        request.arg(IOperationRequest.SUPER_TENANT, false);
        request.arg(IOperationRequest.SUPER_OWNER, false);
        return request;
    }
}
