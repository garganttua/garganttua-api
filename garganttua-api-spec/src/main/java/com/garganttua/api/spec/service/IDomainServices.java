package com.garganttua.api.spec.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.service.pipeline.IPipeline;
import com.garganttua.api.spec.sort.ISort;

/**
 * Service interface for domain operations.
 * Provides CRUD operations and use case invocation through a pipeline-based
 * execution model.
 *
 * @param <E> the entity type
 */
public interface IDomainServices<E> {

    // ========== CRUD Operations ==========

    /**
     * Reads all entities matching the criteria.
     * 
     * @param request  the service request
     * @param pageable optional pagination
     * @param filter   optional filter
     * @param sort     optional sort
     * @return the service response containing a list of entities
     */
    IOperationResponse readAll(IOperationRequest request, Optional<IPageable> pageable,
            Optional<IFilter> filter, Optional<ISort> sort);

    /**
     * Reads a single entity by UUID.
     * 
     * @param request the service request
     * @param uuid    the entity UUID
     * @return the service response containing the entity
     */
    IOperationResponse readOne(IOperationRequest request, String uuid);

    /**
     * Creates a new entity.
     * 
     * @param request    the service request
     * @param entityData the entity data as a map
     * @return the service response containing the created entity
     */
    IOperationResponse createOne(IOperationRequest request, Map<String, Object> entityData);

    /**
     * Updates an existing entity.
     * 
     * @param request    the service request
     * @param uuid       the entity UUID
     * @param updateData the update data as a map
     * @return the service response containing the updated entity
     */
    IOperationResponse updateOne(IOperationRequest request, String uuid, Map<String, Object> updateData);

    /**
     * Deletes a single entity by UUID.
     * 
     * @param request the service request
     * @param uuid    the entity UUID
     * @return the service response
     */
    IOperationResponse deleteOne(IOperationRequest request, String uuid);

    /**
     * Deletes all entities matching the filter.
     * 
     * @param request the service request
     * @param filter  optional filter
     * @return the service response containing the count of deleted entities
     */
    IOperationResponse deleteAll(IOperationRequest request, Optional<IFilter> filter);

    // ========== Use Cases ==========

    /**
     * Invokes a use case by name.
     * 
     * @param useCaseName the use case name
     * @param request     the service request
     * @param args        additional arguments
     * @return the service response
     */
    IOperationResponse invokeUseCase(String useCaseName, IOperationRequest request, Object... args);

    /**
     * Checks if a use case exists.
     * 
     * @param useCaseName the use case name
     * @return true if the use case exists
     */
    boolean hasUseCase(String useCaseName);

    /**
     * Gets the names of all available use cases.
     * 
     * @return list of use case names
     */
    List<String> getUseCaseNames();

    // ========== Direct Script Execution ==========

    /**
     * Executes a script directly, bypassing the pipeline.
     * 
     * @param scriptSource the script source code
     * @param args         arguments to pass to the script
     * @return the service response
     */
    IOperationResponse executeScript(String scriptSource, Object... args);

    // ========== Pipeline Access ==========

    /**
     * Gets the pipeline used by this service.
     * 
     * @return the pipeline
     */
    IPipeline getPipeline();

    /**
     * Sets the pipeline for this service.
     * 
     * @param pipeline the pipeline to use
     */
    void setPipeline(IPipeline pipeline);

    // ========== Domain Info ==========

    /**
     * Gets the domain name.
     * 
     * @return the domain name
     */
    String getDomainName();
}
