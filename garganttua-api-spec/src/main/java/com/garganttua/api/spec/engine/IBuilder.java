package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.CoreException;

public interface IBuilder<Built, Up> {

    Built build() throws CoreException;

    Up up();

}
