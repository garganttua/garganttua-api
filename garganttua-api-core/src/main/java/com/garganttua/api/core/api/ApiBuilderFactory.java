package com.garganttua.api.core.api;

import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IBootstrapBuilderFactory;
import com.garganttua.core.dsl.IBuilder;

/**
 * SPI factory that lets {@code Bootstrap.autoDetect(true)} discover and
 * register an {@link ApiBuilder} without the caller having to declare it
 * via {@code .withBuilder(...)} explicitly.
 *
 * <p>Listed in
 * {@code META-INF/services/com.garganttua.core.dsl.IBootstrapBuilderFactory}.
 *
 * @since 3.0.0-ALPHA01
 */
public final class ApiBuilderFactory implements IBootstrapBuilderFactory {

    @Override
    public IBuilder<?> create() throws DslException {
        return (ApiBuilder) ApiBuilder.builder();
    }
}
