package com.garganttua.api.core.repository;

import com.garganttua.api.commons.ApiException;

public class RepositoryException extends ApiException {

    private static final int REPOSITORY_ERROR_CODE = 200;

    public RepositoryException(String message) {
        super(RepositoryException.REPOSITORY_ERROR_CODE, message);
    }

}
