package com.garganttua.api.core.engine;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.reflection.GGReflectionException;

public class BuilderException extends CoreException{

    public BuilderException(CoreExceptionCode coreGenericCode, String message) {
        super(coreGenericCode, message);
    }

    public BuilderException(CoreExceptionCode coreGenericCode, String message, GGReflectionException e) {
        super(coreGenericCode, message, e);
    }

}
