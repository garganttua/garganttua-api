package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.CoreException;

public interface IBuilder<Built> {

    Built build() throws CoreException;

}
