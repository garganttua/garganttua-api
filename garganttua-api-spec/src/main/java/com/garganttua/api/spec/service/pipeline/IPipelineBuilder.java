package com.garganttua.api.spec.service.pipeline;

import com.garganttua.api.spec.context.BusinessOperation;

/**
 * Builder interface for constructing pipelines with a fluent API.
 */
public interface IPipelineBuilder {

    // ========== Predefined Phases ==========

    /**
     * Adds the PROTOCOL phase with default order.
     * @return this builder for chaining
     */
    IPipelineBuilder withProtocolPhase();

    /**
     * Adds the SECURITY phase with default order.
     * @return this builder for chaining
     */
    IPipelineBuilder withSecurityPhase();

    /**
     * Adds the BUSINESS phase with default order.
     * @return this builder for chaining
     */
    IPipelineBuilder withBusinessPhase();

    /**
     * Adds the RESPONSE phase with default order.
     * @return this builder for chaining
     */
    IPipelineBuilder withResponsePhase();

    // ========== Custom Phases ==========

    /**
     * Adds a custom phase with the given name and order.
     * @param name the phase name
     * @param order the execution order
     * @return this builder for chaining
     */
    IPipelineBuilder withPhase(String name, int order);

    /**
     * Adds a custom phase with the given name, type, and order.
     * @param name the phase name
     * @param type the phase type
     * @param order the execution order
     * @return this builder for chaining
     */
    IPipelineBuilder withPhase(String name, PhaseType type, int order);

    // ========== Scripts ==========

    /**
     * Adds a script to a predefined phase.
     * @param phase the phase type
     * @param scriptPath the script path
     * @param operations the business operations this script applies to
     * @return this builder for chaining
     */
    IPipelineBuilder addScript(PhaseType phase, String scriptPath, BusinessOperation... operations);

    /**
     * Adds a script to a named phase.
     * @param phaseName the phase name
     * @param scriptPath the script path
     * @param operations the business operations this script applies to
     * @return this builder for chaining
     */
    IPipelineBuilder addScript(String phaseName, String scriptPath, BusinessOperation... operations);

    /**
     * Adds a script with a custom order to a predefined phase.
     * @param phase the phase type
     * @param scriptPath the script path
     * @param order the script execution order within the phase
     * @param operations the business operations this script applies to
     * @return this builder for chaining
     */
    IPipelineBuilder addScript(PhaseType phase, String scriptPath, int order, BusinessOperation... operations);

    /**
     * Adds a script with a custom order to a named phase.
     * @param phaseName the phase name
     * @param scriptPath the script path
     * @param order the script execution order within the phase
     * @param operations the business operations this script applies to
     * @return this builder for chaining
     */
    IPipelineBuilder addScript(String phaseName, String scriptPath, int order, BusinessOperation... operations);

    // ========== Configuration ==========

    /**
     * Disables a predefined phase.
     * @param phase the phase type to disable
     * @return this builder for chaining
     */
    IPipelineBuilder disablePhase(PhaseType phase);

    /**
     * Disables a named phase.
     * @param phaseName the phase name to disable
     * @return this builder for chaining
     */
    IPipelineBuilder disablePhase(String phaseName);

    /**
     * Sets the base path for script files.
     * @param basePath the base path
     * @return this builder for chaining
     */
    IPipelineBuilder scriptBasePath(String basePath);

    // ========== Build ==========

    /**
     * Builds the pipeline.
     * @return the configured pipeline
     */
    IPipeline build();

    // ========== Factory ==========

    /**
     * Creates a builder from an existing pipeline.
     * @param pipeline the source pipeline
     * @return a new builder initialized with the pipeline's configuration
     */
    static IPipelineBuilder from(IPipeline pipeline) {
        throw new UnsupportedOperationException("Use implementation class directly");
    }
}
