package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.CoreException;

public interface IAutomaticBuilder<Builder, Built> extends IBuilder<Built> {

    Builder autoDetect(boolean b) throws CoreException;

}
