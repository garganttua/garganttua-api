package com.garganttua.api.spec.service;

import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.service.pipeline.IPipeline;

/**
 * Factory for creating IDomainServices instances.
 */
public interface IDomainServicesFactory {

    /**
     * Creates a domain services instance with the default pipeline.
     * @param domainContext the domain context
     * @param <E> the entity type
     * @return the domain services instance
     */
    <E> IDomainServices<E> create(IDomainContext<E> domainContext);

    /**
     * Creates a domain services instance with a custom pipeline.
     * @param domainContext the domain context
     * @param pipeline the pipeline to use
     * @param <E> the entity type
     * @return the domain services instance
     */
    <E> IDomainServices<E> create(IDomainContext<E> domainContext, IPipeline pipeline);
}
