package com.garganttua.api.core.context;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;

public class ContextException extends CoreException {

    public ContextException(String message) {
        super(CoreExceptionCode.CONTEXT_ERROR, message);
    }

}
