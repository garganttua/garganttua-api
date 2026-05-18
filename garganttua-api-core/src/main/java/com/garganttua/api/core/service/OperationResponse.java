package com.garganttua.api.core.service;

import java.time.Duration;

import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;

import lombok.Getter;

@Getter
public class OperationResponse implements IOperationResponse {

    private final OperationResponseCode responseCode;
    private final Object response;
    private final Duration processingTime;

    public OperationResponse(OperationResponseCode responseCode, Object response) {
        this(responseCode, response, null);
    }

    public OperationResponse(OperationResponseCode responseCode, Object response, Duration processingTime) {
        this.responseCode = responseCode;
        this.response = response;
        this.processingTime = processingTime;
    }

    /**
     * Returns a copy of this response stamped with the given processing time.
     * Used by {@code Domain.invoke()} to attach an end-to-end timing to the
     * response right before returning, without mutating the existing
     * instance. Pass {@code null} to clear the timing.
     */
    public OperationResponse withProcessingTime(Duration processingTime) {
        return new OperationResponse(this.responseCode, this.response, processingTime);
    }

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
        StringBuilder sb = new StringBuilder("OperationResponse{code=")
                .append(responseCode)
                .append(", response=")
                .append(response);
        if (processingTime != null) {
            sb.append(", processingTime=").append(processingTime.toMillis()).append("ms");
        }
        return sb.append("}").toString();
    }
}
