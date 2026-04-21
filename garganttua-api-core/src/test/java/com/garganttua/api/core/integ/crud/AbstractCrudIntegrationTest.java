package com.garganttua.api.core.integ.crud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.garganttua.api.core.builder.ApiBuilder;
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
    }

    // ───── Non-tenant entity: Product ─────

    public static class Product {
        private String id;
        private String uuid;
        private String tenantId;
        private String label;
        private double price;

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
            return new ArrayList<>(storage);
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
            if (filter.isPresent()) {
                return filterStorage(filter.get());
            }
            return new ArrayList<>(storage);
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
                            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
                            field.setAccessible(true);
                            Object actual = field.get(obj);
                            if (expected != null && expected.equals(actual)) {
                                result.add(obj);
                            }
                        } catch (Exception e) { /* skip */ }
                    }
                    return result;
                }
            }
            return list;
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
        expressionContextBuilder.withPackage("com.garganttua.api.core.expression");
        ((IDependentBuilder<IExpressionContextBuilder, ?>) expressionContextBuilder).provide(injectionContextBuilder);
        expressionContextBuilder.build();

        ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(reflectionBuilder);
        ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(injectionContextBuilder);
        ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(expressionContextBuilder);

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
}
