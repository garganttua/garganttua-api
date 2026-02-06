package com.garganttua.api.core.context;

import com.garganttua.api.spec.service.IServiceResponse;
import com.garganttua.api.spec.service.ServiceResponseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServiceResponse implements IServiceResponse {

    private final ServiceResponseCode responseCode;
    private final Object response;

    public static ServiceResponse ok(Object data) {
        return new ServiceResponse(ServiceResponseCode.OK, data);
    }

    public static ServiceResponse created(Object entity) {
        return new ServiceResponse(ServiceResponseCode.CREATED, entity);
    }

    public static ServiceResponse updated(Object entity) {
        return new ServiceResponse(ServiceResponseCode.UPDATED, entity);
    }

    public static ServiceResponse deleted(Object data) {
        return new ServiceResponse(ServiceResponseCode.DELETED, data);
    }

    public static ServiceResponse notFound(String message) {
        return new ServiceResponse(ServiceResponseCode.NOT_FOUND, message);
    }

    public static ServiceResponse badRequest(String message) {
        return new ServiceResponse(ServiceResponseCode.CLIENT_ERROR, message);
    }

    public static ServiceResponse error(String message) {
        return new ServiceResponse(ServiceResponseCode.SERVER_ERROR, message);
    }

    public static ServiceResponse unauthorized(String message) {
        return new ServiceResponse(ServiceResponseCode.UNAUTHORIZED, message);
    }

    public static ServiceResponse forbidden(String message) {
        return new ServiceResponse(ServiceResponseCode.FORBIDDEN, message);
    }

    public static ServiceResponse notAvailable(String message) {
        return new ServiceResponse(ServiceResponseCode.NOT_AVAILABLE, message);
    }

    @Override
    public String toString() {
        return "ServiceResponse{code=" + responseCode + ", response=" + response + "}";
    }
}
