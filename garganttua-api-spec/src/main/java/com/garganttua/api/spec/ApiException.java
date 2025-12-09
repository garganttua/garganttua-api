package com.garganttua.api.spec;

import com.garganttua.core.CoreException;

public class ApiException extends CoreException {

    public static final int API_ERROR_CODE = 100;

    protected ApiException(String message) {
        super(API_ERROR_CODE, message);
    }

}
