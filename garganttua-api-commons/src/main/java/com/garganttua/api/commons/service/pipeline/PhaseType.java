package com.garganttua.api.commons.service.pipeline;

/**
 * Predefined phase types for the service pipeline.
 * Each phase has a default order that determines execution sequence.
 */
public enum PhaseType {

    /**
     * Protocol parsing and input validation phase.
     * Examples: HTTP parsing, gRPC parsing, input validation.
     */
    PROTOCOL(100),

    /**
     * Security phase for authentication and authorization.
     * Examples: auth check, tenant filtering, owner verification.
     */
    SECURITY(200),

    /**
     * Business logic phase for CRUD operations and use cases.
     * Examples: repository calls, entity hooks, business rules.
     */
    BUSINESS(300),

    /**
     * Response formatting and post-processing phase.
     * Examples: JSON formatting, event publishing, response transformation.
     */
    RESPONSE(400),

    /**
     * Custom phase type for user-defined phases.
     * Order must be specified when creating the phase.
     */
    CUSTOM(0);

    private final int defaultOrder;

    PhaseType(int defaultOrder) {
        this.defaultOrder = defaultOrder;
    }

    public int getDefaultOrder() {
        return this.defaultOrder;
    }
}
