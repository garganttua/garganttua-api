package com.garganttua.api.spec.engine;

public interface IObjectSupplierBuilder<SuppliedObjectType> extends IBuilder<IObjectSupplier<SuppliedObjectType>> {

    Class<SuppliedObjectType> getObjectClass();

}
