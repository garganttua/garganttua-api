package com.garganttua.api.commons.service.pipeline;

import java.util.List;

/**
 * Represents a phase in the service execution pipeline.
 * A phase contains one or more scripts that execute in order.
 */
public interface IPhase {

    /**
     * Gets the name of this phase.
     * @return the phase name
     */
    String getName();

    /**
     * Gets the type of this phase.
     * @return the phase type
     */
    PhaseType getType();

    /**
     * Gets the execution order of this phase.
     * Lower values execute first.
     * @return the order value
     */
    int getOrder();

    /**
     * Gets the scripts associated with this phase.
     * Scripts are returned in execution order.
     * @return list of phase scripts
     */
    List<IPhaseScript> getScripts();

    /**
     * Checks if this phase is enabled.
     * @return true if the phase should be executed
     */
    boolean isEnabled();
}
