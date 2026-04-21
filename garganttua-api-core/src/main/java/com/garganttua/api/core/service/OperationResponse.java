package com.garganttua.api.core.service;

import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OperationResponse implements IOperationResponse {

    private final OperationResponseCode responseCode;
    private final Object response;

    public static OperationResponse ok(Object data) {
        return new OperationResponse(OperationResponseCode.OK, data);
    }

    public static OperationResponse created(Object entity) {
        return new OperationResponse(OperationResponseCode.CREATED, entity);
    }

    public static OperationResponse updated(Object entity) {
        return new OperationResponse(OperationResponseCode.UPDATED, entity);
    }

    public static OperationResponse deleted(Object data) {
        return new OperationResponse(OperationResponseCode.DELETED, data);
    }

    public static OperationResponse notFound(String message) {
        return new OperationResponse(OperationResponseCode.NOT_FOUND, message);
    }

    public static OperationResponse badRequest(String message) {
        return new OperationResponse(OperationResponseCode.CLIENT_ERROR, message);
    }

    public static OperationResponse error(String message) {
        return new OperationResponse(OperationResponseCode.SERVER_ERROR, message);
    }

    public static OperationResponse unauthorized(String message) {
        return new OperationResponse(OperationResponseCode.UNAUTHORIZED, message);
    }

    public static OperationResponse forbidden(String message) {
        return new OperationResponse(OperationResponseCode.FORBIDDEN, message);
    }

    public static OperationResponse notAvailable(String message) {
        return new OperationResponse(OperationResponseCode.NOT_AVAILABLE, message);
    }

    @Override
    public String toString() {
        return "OperationResponse{code=" + responseCode + ", response=" + response + "}";
    }
}
