package com.garganttua.api.core.context.application.supplier;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.reflection.GGReflectionException;

public class SupplyException extends CoreException {

    public SupplyException(String msg) {
        super(CoreExceptionCode.SUPPLY_ERROR, msg);
    }

    public SupplyException(Exception e) {
        super(CoreExceptionCode.SUPPLY_ERROR, e.getMessage(), e);
    }

    public SupplyException(String string, GGReflectionException e) {
        super(CoreExceptionCode.SUPPLY_ERROR, string, e);
    }

}
