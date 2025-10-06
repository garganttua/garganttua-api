package com.garganttua.api.core.context.execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.garganttua.api.core.builder.supplier.ExecutionContextObjectSupplierBuilder;
import com.garganttua.api.core.runtime.ExecutionVariable;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.engine.BusinessOperation;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.IMethodBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IServiceSecurityRequirements;
import com.garganttua.api.spec.engine.ISupplyObject;
import com.garganttua.api.spec.engine.Service;
import com.garganttua.api.spec.engine.ServiceType;
import com.garganttua.api.spec.engine.StandardParameter;

import lombok.Getter;
import lombok.Setter;

public final class ExecutionContext implements IExecutionContext {

    @Getter
    private @Nonnull InputRequest inputRequest;

    private IServiceSecurityRequirements securityRequirements;

    @Setter
    private Object response;

    private Map<String, Object> executionVariables;

    public record InputRequest(
            Object inputRawRequest,
            String tenantUuid,
            String requestedTenantUuid,
            String domainName,
            Service service,
            Byte[] body,
            Map<String, Object> parameters) {

        Optional<?> getParameter(StandardParameter parameter) {
            if (parameters == null)
                return Optional.empty();
            return Optional.ofNullable(this.parameters.get(parameter.name()));
        }

        Optional<?> getParameter(String parameterName) {
            if (parameters == null)
                return Optional.empty();
            return Optional.ofNullable(this.parameters.get(parameterName));
        }
    }

    public ExecutionContext(InputRequest request) {
        this.inputRequest = Objects.requireNonNull(request, "Input Request cannot be null");
        Objects.requireNonNull(request.domainName, "Domain name cannot be null");
        Objects.requireNonNull(request.service, "Service cannot be null");

        if (request.parameters != null)
            this.executionVariables = new HashMap<>(request.parameters);
        else
            this.executionVariables = new HashMap<>();
    }

    @Override
    public boolean isStandard() {
        return this.inputRequest.service.serviceType().equals(ServiceType.standard);
    }

    @Override
    public boolean isUseCase() {
        return ServiceType.usesCase.equals(this.inputRequest.service.serviceType());
    }

    @Override
    public boolean isAuthentication() {
        return ServiceType.authentication.equals(this.inputRequest.service.serviceType());
    }

    @Override
    public String getDomainName() {
        return this.inputRequest.domainName;
    }

    public class Suppliers {

        public static <T> IObjectSupplierBuilder<T> entity(Class<T> entityClass) {
            ISupplyObject<T, IExecutionContext> supply = (context) -> {
                return (Optional<T>) context.getExecutionVariable(ExecutionVariable.REPOSITORY_RETURN.toString());
            };

            IObjectSupplierBuilder<T> builder = new ExecutionContextObjectSupplierBuilder<>(supply,
                    entityClass);

            return builder;
        }

        public static <T> IObjectSupplierBuilder<T> authorization(Class<T> authorizationClass) {
            ISupplyObject<T, IExecutionContext> supply = (context) -> Optional.empty();

            IObjectSupplierBuilder<T> builder = new ExecutionContextObjectSupplierBuilder<>(supply,
                    authorizationClass);

            return builder;
        }

        public static IObjectSupplierBuilder<Byte[]> authorization() {
            ISupplyObject<Byte[], IExecutionContext> supply = (context) -> Optional.empty();

            IObjectSupplierBuilder<Byte[]> builder = new ExecutionContextObjectSupplierBuilder<>(supply,
                    Byte[].class);

            return builder;
        }

        public static <T> IObjectSupplierBuilder<T> key(Class<T> keyClass) {
            ISupplyObject<T, IExecutionContext> supply = (context) -> Optional.empty();

            IObjectSupplierBuilder<T> builder = new ExecutionContextObjectSupplierBuilder<>(supply,
                    keyClass);

            return builder;
        }

        public static IObjectSupplierBuilder<ICaller> caller() {
            ISupplyObject<ICaller, IExecutionContext> supply = (context) -> Optional.empty();

            IObjectSupplierBuilder<ICaller> builder = new ExecutionContextObjectSupplierBuilder<>(supply,
                    ICaller.class);

            return builder;
        }

        public static IObjectSupplierBuilder<Byte[]> credentials() {
            ISupplyObject<Byte[], IExecutionContext> supply = (context) -> Optional.empty();

            IObjectSupplierBuilder<Byte[]> builder = new ExecutionContextObjectSupplierBuilder<>(supply,
                    Byte[].class);

            return builder;
        }

        public static IObjectSupplierBuilder<Object> principal() {
            ISupplyObject<Object, IExecutionContext> supply = (context) -> Optional.empty();

            IObjectSupplierBuilder<Object> builder = new ExecutionContextObjectSupplierBuilder<>(supply,
                    Object.class);

            return builder;
        }

        public static IObjectSupplierBuilder<Object> technicalRequest() {
            ISupplyObject<Object, IExecutionContext> supply = (context) -> Optional.empty();

            IObjectSupplierBuilder<Object> builder = new ExecutionContextObjectSupplierBuilder<>(supply,
                    Object.class);

            return builder;
        }

        public static IObjectSupplierBuilder<Object> technicalResponse() {
            ISupplyObject<Object, IExecutionContext> supply = (context) -> Optional.empty();

            IObjectSupplierBuilder<Object> builder = new ExecutionContextObjectSupplierBuilder<>(supply,
                    Object.class);

            return builder;
        }

    }

    @Override
    public IServiceSecurityRequirements getSecurityRequirements() {
        return this.securityRequirements;
    }

    @Override
    public String getServiceName() {
        return this.inputRequest.service.getServiceName();
    }

    @Override
    public void setSecurityRequirements(IServiceSecurityRequirements sReq) {
        this.securityRequirements = Objects.requireNonNull(sReq, "Security requirements cannot be null");
    }

    @Override
    public Optional<?> getResponse() {
        return Optional.ofNullable(this.response);
    }

    @Override
    public BusinessOperation getBusinessOperation() {
        return this.inputRequest.service.getBusinessOperation();
    }

    @Override
    public Optional<?> getInputParameter(StandardParameter parameter) {
        return this.inputRequest.getParameter(parameter);
    }

    @Override
    public Optional<?> getInputParameter(String parameterName) {
        return this.inputRequest.getParameter(parameterName);
    }

    @Override
    public <T> Optional<T> getInputParameter(StandardParameter parameter, Class<T> clazz) {
        Optional<?> value = this.inputRequest.getParameter(parameter);
        if (value.isPresent() && clazz.isAssignableFrom(value.get().getClass())) {
            return (Optional<T>) value;
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> getInputParameter(String parameterNamer, Class<T> clazz) {
        Optional<?> value = this.inputRequest.getParameter(parameterNamer);
        if (value.isPresent() && clazz.isAssignableFrom(value.get().getClass())) {
            return (Optional<T>) value;
        }
        return Optional.empty();
    }

    @Override
    public Optional<?> getExecutionVariable(String variableName) {
        return Optional.ofNullable(this.executionVariables.get(variableName));
    }

    @Override
    public <T> Optional<T> getExecutionVariable(String variableName, Class<T> clazz) {
        Object var = this.executionVariables.get(variableName);
        if (var != null && clazz.isAssignableFrom(var.getClass()))
            return (Optional<T>) Optional.of(var);
        return Optional.empty();
    }

    @Override
    public void setExecutionVariable(String variableName, Object variable) {
        this.executionVariables.put(variableName, variable);
    }

    @Override
    public void setAfterOperationMethods(List<IMethodBinderBuilder<?, ?>> methods) {
        this.executionVariables.put(ExecutionVariable.AFTER_METHODS.toString(), methods);
    }

    @Override
    public void setBeforeOperationMethods(List<IMethodBinderBuilder<?, ?>> methods) {
        this.executionVariables.put(ExecutionVariable.BEFORE_METHODS.toString(), methods);
    }

    @Override
    public List<IMethodBinderBuilder<?, ?>> getAfterOperationMethods() {
        Object methods = this.executionVariables.get(ExecutionVariable.AFTER_METHODS.toString());
        if (methods != null)
            return (List<IMethodBinderBuilder<?, ?>>) methods;
        else
            return List.of();
    }

    @Override
    public List<IMethodBinderBuilder<?, ?>> getBeforeOperationMethods() {
        Object methods = this.executionVariables.get(ExecutionVariable.BEFORE_METHODS.toString());
        if (methods != null)
            return (List<IMethodBinderBuilder<?, ?>>) methods;
        else
            return List.of();
    }
}
