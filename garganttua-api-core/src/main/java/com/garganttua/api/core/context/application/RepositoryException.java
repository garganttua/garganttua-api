package com.garganttua.api.core.context.application;

import com.garganttua.core.CoreException;

public class RepositoryException extends CoreException {

    private static final int REPOSITORY_ERROR_CODE = 200;

    public RepositoryException(String message) {
        super(RepositoryException.REPOSITORY_ERROR_CODE, message);
    }

}
