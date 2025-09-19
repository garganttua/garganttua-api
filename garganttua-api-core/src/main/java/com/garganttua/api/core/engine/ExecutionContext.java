package com.garganttua.api.core.engine;

import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.ISupplyObject;

public class ExecutionContext implements IExecutionContext {

    public class Suppliers {

        public static IObjectSupplierBuilder<Object> entity() {
            ISupplyObject<Object, IApplicationContext> supply = null;

            IObjectSupplierBuilder<Object> builder = new ApplicationContextObjectSupplierBuilder<>(supply,
                    Object.class);

            return builder;
        }

        public static IObjectSupplierBuilder<Object> authorization() {
            ISupplyObject<Object, IApplicationContext> supply = null;

            IObjectSupplierBuilder<Object> builder = new ApplicationContextObjectSupplierBuilder<>(supply,
                    Object.class);

            return builder;
        }

        public static IObjectSupplierBuilder<Object> key() {
            ISupplyObject<Object, IApplicationContext> supply = null;

            IObjectSupplierBuilder<Object> builder = new ApplicationContextObjectSupplierBuilder<>(supply,
                    Object.class);

            return builder;
        }

        public static IObjectSupplierBuilder<ICaller> caller() {
            ISupplyObject<ICaller, IApplicationContext> supply = null;

            IObjectSupplierBuilder<ICaller> builder = new ApplicationContextObjectSupplierBuilder<>(supply,
                    ICaller.class);

            return builder;
        }

    }

}
