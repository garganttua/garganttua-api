package com.garganttua.api.core.builder;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;

public class BuilderException extends CoreException{

    public BuilderException(String message) {
        super(CoreExceptionCode.BUILDER_CODE, message);
    }

    public BuilderException(String message, Exception e) {
        super(CoreExceptionCode.BUILDER_CODE, message, e);
    }

}
