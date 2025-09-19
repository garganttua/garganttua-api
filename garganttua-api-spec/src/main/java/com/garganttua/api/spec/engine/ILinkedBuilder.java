package com.garganttua.api.spec.engine;

public interface ILinkedBuilder<Built, Up> extends IBuilder<Built> {

    Up up();

}
