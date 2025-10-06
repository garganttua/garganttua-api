package com.garganttua.api.spec.engine;

public interface IContextualObjectSupplier<Supplied, Context> extends IObjectSupplier<Supplied> {

    Class<Context> getContextClass();

}
