package com.garganttua.api.commons.service.pipeline;

import java.util.Set;

import com.garganttua.api.commons.operation.BusinessOperation;

/**
 * Represents a script that executes within a pipeline phase.
 * Scripts can be targeted to specific business operations.
 */
public interface IPhaseScript {

    /**
     * Gets the name of this script.
     * @return the script name
     */
    String getName();

    /**
     * Gets the path to the script file (.gs).
     * @return the script path relative to the scripts base directory
     */
    String getScriptPath();

    /**
     * Gets the business operations this script applies to.
     * If empty, the script applies to all operations.
     * @return set of applicable business operations
     */
    Set<BusinessOperation> getOperations();

    /**
     * Gets the execution order within the phase.
     * Lower values execute first.
     * @return the order value
     */
    int getOrder();

    /**
     * Checks if this script is enabled.
     * @return true if the script should be executed
     */
    boolean isEnabled();

    /**
     * Checks if this script applies to the given operation.
     * @param operation the business operation to check
     * @return true if the script should execute for this operation
     */
    default boolean appliesTo(BusinessOperation operation) {
        Set<BusinessOperation> ops = getOperations();
        return ops == null || ops.isEmpty() || ops.contains(operation);
    }
}
