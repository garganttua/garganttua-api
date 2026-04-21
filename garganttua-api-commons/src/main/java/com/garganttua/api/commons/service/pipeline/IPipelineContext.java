package com.garganttua.api.commons.service.pipeline;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.core.reflection.IClass;

/**
 * Context shared between pipeline phases during execution.
 * Provides access to request/response, caller information, and shared data.
 */
public interface IPipelineContext {

    // ========== Request/Response ==========

    /**
     * Gets the service request.
     * @return the current request
     */
    IOperationRequest getRequest();

    /**
     * Sets the service request (can be modified by phases).
     * @param request the request
     */
    void setRequest(IOperationRequest request);

    /**
     * Gets the service response.
     * @return the current response, or null if not yet set
     */
    IOperationResponse getResponse();

    /**
     * Sets the service response.
     * @param response the response
     */
    void setResponse(IOperationResponse response);

    // ========== Business Operation ==========

    /**
     * Gets the business operation being executed.
     * @return the business operation
     */
    BusinessOperation getBusinessOperation();

    // ========== Shared Data ==========

    /**
     * Gets a value from the context.
     * @param key the key
     * @param type the expected type
     * @param <T> the type parameter
     * @return the value, or null if not present
     */
    <T> T get(String key, IClass<T> type);

    /**
     * Sets a value in the context.
     * @param key the key
     * @param value the value
     */
    void set(String key, Object value);

    /**
     * Checks if a key exists in the context.
     * @param key the key
     * @return true if the key exists
     */
    boolean has(String key);

    /**
     * Removes a value from the context.
     * @param key the key
     */
    void remove(String key);

    // ========== Pipeline Control ==========

    /**
     * Aborts the pipeline execution with the given response.
     * No further phases will be executed.
     * @param response the response to return
     */
    void abort(IOperationResponse response);

    /**
     * Checks if the pipeline has been aborted.
     * @return true if aborted
     */
    boolean isAborted();

    /**
     * Skips execution to a specific phase.
     * All phases before the target phase will be skipped.
     * @param phase the target phase type
     */
    void skipToPhase(PhaseType phase);

    /**
     * Gets the phase to skip to, if any.
     * @return the target phase type, or null if no skip requested
     */
    PhaseType getSkipToPhase();

    /**
     * Clears the skip-to-phase request.
     */
    void clearSkipToPhase();

    // ========== Domain Context ==========

    /**
     * Gets the domain context for the current operation.
     * @return the domain context
     */
    IDomain<?> getDomain();

    // ========== Caller ==========

    /**
     * Gets the caller for the current request.
     * @return the caller, or null if not yet authenticated
     */
    ICaller getCaller();

    /**
     * Sets the caller (typically set by security phase).
     * @param caller the caller
     */
    void setCaller(ICaller caller);

    // ========== Operation Arguments ==========

    /**
     * Gets the arguments passed to the operation.
     * @return array of arguments
     */
    Object[] getArguments();

    /**
     * Sets the operation arguments.
     * @param arguments the arguments
     */
    void setArguments(Object[] arguments);

    /**
     * Gets an argument by index.
     * @param index the argument index
     * @param <T> the expected type
     * @return the argument value
     */
    @SuppressWarnings("unchecked")
    default <T> T getArgument(int index) {
        Object[] args = getArguments();
        if (args == null || index < 0 || index >= args.length) {
            return null;
        }
        return (T) args[index];
    }
}
