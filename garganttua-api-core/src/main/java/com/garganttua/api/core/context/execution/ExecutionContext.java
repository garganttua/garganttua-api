package com.garganttua.api.core.context.execution;

import com.garganttua.core.supply.IObjectSupplier;

/**
 * Execution context utility class providing suppliers for runtime execution.
 */
public class ExecutionContext {

    /**
     * Suppliers for common execution context objects.
     */
    public static class Suppliers {

        /**
         * Creates a supplier for entity objects.
         *
         * @param <E> the entity type
         * @param entityClass the entity class
         * @return an object supplier for the entity
         */
        public static <E> IObjectSupplier<E> entity(Class<E> entityClass) {
            return new ContextObjectSupplier<>(entityClass, "entity");
        }

        /**
         * Creates a supplier for input objects.
         *
         * @param <I> the input type
         * @param inputClass the input class
         * @return an object supplier for the input
         */
        public static <I> IObjectSupplier<I> input(Class<I> inputClass) {
            return new ContextObjectSupplier<>(inputClass, "input");
        }

        /**
         * Creates a supplier for output objects.
         *
         * @param <O> the output type
         * @param outputClass the output class
         * @return an object supplier for the output
         */
        public static <O> IObjectSupplier<O> output(Class<O> outputClass) {
            return new ContextObjectSupplier<>(outputClass, "output");
        }

        /**
         * Creates a supplier for caller objects.
         *
         * @return an object supplier for the caller
         */
        public static IObjectSupplier<Object> caller() {
            return new ContextObjectSupplier<>(Object.class, "caller");
        }

        /**
         * Creates a supplier for context objects.
         *
         * @return an object supplier for the context
         */
        public static IObjectSupplier<Object> context() {
            return new ContextObjectSupplier<>(Object.class, "context");
        }
    }
}
