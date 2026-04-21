package com.garganttua.api.commons.service.pipeline;

import java.util.List;

import com.garganttua.api.commons.service.IOperationResponse;

/**
 * Represents a service execution pipeline composed of ordered phases.
 * The pipeline processes requests through phases and produces responses.
 */
public interface IPipeline {

    /**
     * Gets all phases in execution order.
     * @return list of phases sorted by order
     */
    List<IPhase> getPhases();

    /**
     * Gets a phase by its type.
     * @param type the phase type
     * @return the phase, or null if not found
     */
    IPhase getPhase(PhaseType type);

    /**
     * Gets a phase by its name.
     * @param name the phase name
     * @return the phase, or null if not found
     */
    IPhase getPhase(String name);

    /**
     * Executes the pipeline with the given context.
     * @param context the pipeline context
     * @return the service response
     */
    IOperationResponse execute(IPipelineContext context);

    /**
     * Adds a phase to the pipeline.
     * @param phase the phase to add
     * @return this pipeline for chaining
     */
    IPipeline addPhase(IPhase phase);

    /**
     * Removes a phase by name.
     * @param name the phase name
     * @return this pipeline for chaining
     */
    IPipeline removePhase(String name);

    /**
     * Enables or disables a phase by name.
     * @param name the phase name
     * @param enabled true to enable, false to disable
     * @return this pipeline for chaining
     */
    IPipeline enablePhase(String name, boolean enabled);

    /**
     * Creates a copy of this pipeline.
     * @return a new pipeline with the same configuration
     */
    IPipeline copy();
}
