package com.garganttua.api.core.service;

import java.time.Duration;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;

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

    public OperationResponseCode getResponseCode() { return this.responseCode; }
    public Object getResponse() { return this.response; }
    public Duration getProcessingTime() { return this.processingTime; }

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

    public static OperationResponse notFound(Throwable cause) {
        return new OperationResponse(OperationResponseCode.NOT_FOUND, cause);
    }

    public static OperationResponse notFound(String message) {
        return notFound(new ApiException(message));
    }

    public static OperationResponse badRequest(Throwable cause) {
        return new OperationResponse(OperationResponseCode.CLIENT_ERROR, cause);
    }

    public static OperationResponse badRequest(String message) {
        return badRequest(new ApiException(message));
    }

    public static OperationResponse error(Throwable cause) {
        return new OperationResponse(OperationResponseCode.SERVER_ERROR, cause);
    }

    public static OperationResponse error(String message) {
        return error(new ApiException(message));
    }

    public static OperationResponse unauthorized(Throwable cause) {
        return new OperationResponse(OperationResponseCode.UNAUTHORIZED, cause);
    }

    public static OperationResponse unauthorized(String message) {
        return unauthorized(new ApiException(message));
    }

    public static OperationResponse forbidden(Throwable cause) {
        return new OperationResponse(OperationResponseCode.FORBIDDEN, cause);
    }

    public static OperationResponse forbidden(String message) {
        return forbidden(new ApiException(message));
    }

    public static OperationResponse notAvailable(Throwable cause) {
        return new OperationResponse(OperationResponseCode.NOT_AVAILABLE, cause);
    }

    public static OperationResponse notAvailable(String message) {
        return notAvailable(new ApiException(message));
    }

    public static OperationResponse conflict(Throwable cause) {
        return new OperationResponse(OperationResponseCode.CONFLICT, cause);
    }

    public static OperationResponse conflict(String message) {
        return conflict(new ApiException(message));
    }

    public static OperationResponse notAcceptable(Throwable cause) {
        return new OperationResponse(OperationResponseCode.NOT_ACCEPTABLE, cause);
    }

    public static OperationResponse notAcceptable(String message) {
        return notAcceptable(new ApiException(message));
    }

    public static OperationResponse unsupportedMediaType(Throwable cause) {
        return new OperationResponse(OperationResponseCode.UNSUPPORTED_MEDIA_TYPE, cause);
    }

    public static OperationResponse unsupportedMediaType(String message) {
        return unsupportedMediaType(new ApiException(message));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("OperationResponse{code=")
                .append(responseCode)
                .append(", response=");
        if (response instanceof Throwable t) {
            // Compact, single-line representation. Full stack lives on the
            // Throwable for callers that want it.
            sb.append(t.getClass().getSimpleName())
                    .append(": ")
                    .append(t.getMessage());
        } else {
            sb.append(response);
        }
        if (processingTime != null) {
            sb.append(", processingTime=").append(processingTime.toMillis()).append("ms");
        }
        return sb.append("}").toString();
    }
}
