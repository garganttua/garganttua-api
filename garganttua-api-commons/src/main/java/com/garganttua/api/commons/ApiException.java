package com.garganttua.api.commons;

import com.garganttua.core.CoreException;

public class ApiException extends CoreException {

    public static final int API_ERROR_CODE = 100;

    public ApiException(String message) {
        super(API_ERROR_CODE, message);
    }

    public ApiException(String message, Throwable cause) {
        super(API_ERROR_CODE, message, cause);
    }

    public ApiException(Throwable cause) {
        super(cause instanceof CoreException ? ((CoreException) cause).getCode() : API_ERROR_CODE,
              cause.getMessage(), cause);
    }

    protected ApiException(int code, String message) {
        super(code, message);
    }

    protected ApiException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public static ApiException wrap(Throwable e) {
        if (e instanceof ApiException) {
            return (ApiException) e;
        }
        return new ApiException(e);
    }

}
