package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.CoreException;

public interface IAutomaticLinkedBuilder<Built, Up, Builder> extends IBuilder<Built> {

    Builder autoDetect(boolean b) throws CoreException;

    Up up();

}
