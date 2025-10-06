package com.garganttua.api.core.context.application;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;

public class RepositoryException extends CoreException {

    public RepositoryException(String message) {
        super(CoreExceptionCode.REPOSITORY_ERROR, message);
    }

}
