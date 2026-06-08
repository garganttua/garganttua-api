package com.garganttua.api.core.aot;

import com.garganttua.core.aot.commons.IAOTInfrastructureSeed;
import com.garganttua.core.aot.commons.IAOTSeedContext;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.api.Api;
import com.garganttua.api.core.security.AuthoritiesEndpoint;
import com.garganttua.api.core.domain.Domain;
import com.garganttua.api.core.security.key.DomainKeyContext;
import com.garganttua.api.core.dto.DtoContext;
import com.garganttua.api.core.entity.EntityContext;
import com.garganttua.api.core.entity.EntityUpdater;
import com.garganttua.api.core.usecase.UseCase;
import com.garganttua.api.core.security.AccessRule;
import com.garganttua.api.core.security.authentication.AuthenticationDefinition;
import com.garganttua.api.core.security.authenticator.AuthenticatorDefintion;
import com.garganttua.api.core.security.authenticator.DomainAuthenticatorAuthorizationDefinition;
import com.garganttua.api.core.security.authenticator.DomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.core.security.authorization.DomainAuthorizationDefinition;
import com.garganttua.api.core.domain.DomainDefinition;
import com.garganttua.api.core.security.key.DomainKeyDefinition;
import com.garganttua.api.core.security.DomainSecurityDefinition;
import com.garganttua.api.core.dto.DtoDefinition;
import com.garganttua.api.core.entity.EntityDefinition;
import com.garganttua.api.core.usecase.UseCaseDefinition;
import com.garganttua.api.core.domain.WorkflowDefinition;
import com.garganttua.api.core.expression.ApiExpressions;
import com.garganttua.api.core.expression.AuthorizationProtocolExpressions;
import com.garganttua.api.core.expression.CrudExpressions;
import com.garganttua.api.core.expression.EntityLifecycleExpressions;
import com.garganttua.api.core.expression.ProtocolExpressions;
import com.garganttua.api.core.expression.SecurityExpressions;
import com.garganttua.api.core.expression.SerializationExpressions;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.repository.Repository;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.core.service.OperationResponse;
import com.garganttua.api.core.service.RequestBuilder;

/**
 * Pre-registers every framework-public concrete type of
 * {@code garganttua-api-core} in the {@code AOTRegistry} on cold-start, so
 * user-side {@code @Reflected} classes (or framework-emitted AOT descriptors
 * for the @Reflected DSL builders) that reference these types can still be
 * resolved at runtime in pure-AOT mode — i.e. when
 * {@code garganttua-runtime-reflection} is absent and
 * {@code AOTReflectionProvider} is the only provider.
 *
 * <p>Coverage:
 * <ul>
 *   <li>Runtime context classes (Api, Domain, EntityContext, DtoContext,
 *       UseCase, AuthoritiesEndpoint, DomainKeyContext, EntityUpdater,
 *       Caller).</li>
 *   <li>Immutable definitions (the *Definition records produced by the
 *       builders at build time — DomainDefinition, EntityDefinition,
 *       DtoDefinition, AuthenticatorDefintion (note: typo in the source
 *       filename is intentional, kept for binary compatibility), etc.).</li>
 *   <li>Service primitives (OperationRequest, OperationResponse,
 *       RequestBuilder).</li>
 *   <li>Filter, Repository, DefaultMapper.</li>
 * </ul>
 *
 * <p>Discovered via {@link java.util.ServiceLoader} from
 * {@code META-INF/services/com.garganttua.core.aot.commons.IAOTInfrastructureSeed}.
 *
 * @since 3.0.0-ALPHA01
 */
public class ApiCoreInfrastructureSeed implements IAOTInfrastructureSeed {

    @Override
    public void seed(IAOTSeedContext context) {
        // Runtime context classes — concrete IXxx implementations.
        context.registerClass(Api.class);
        context.registerClass(AuthoritiesEndpoint.class);
        context.registerClass(Caller.class);
        context.registerClass(Domain.class);
        context.registerClass(DomainKeyContext.class);
        context.registerClass(DtoContext.class);
        context.registerClass(EntityContext.class);
        context.registerClass(EntityUpdater.class);
        context.registerClass(UseCase.class);

        // Immutable definitions — produced by the @Reflected DSL builders at
        // build time and referenced from the Domain context they back.
        context.registerClass(AccessRule.class);
        context.registerClass(AuthenticationDefinition.class);
        context.registerClass(AuthenticatorDefintion.class);
        context.registerClass(DomainAuthenticatorAuthorizationDefinition.class);
        context.registerClass(DomainAuthenticatorAuthorizationKeyDefinition.class);
        context.registerClass(DomainAuthorizationDefinition.class);
        context.registerClass(DomainDefinition.class);
        context.registerClass(DomainKeyDefinition.class);
        context.registerClass(DomainSecurityDefinition.class);
        context.registerClass(DtoDefinition.class);
        context.registerClass(EntityDefinition.class);
        context.registerClass(UseCaseDefinition.class);
        context.registerClass(WorkflowDefinition.class);

        // Service primitives — the request/response carriers that flow
        // through Domain.invoke and beyond.
        context.registerClass(OperationRequest.class);
        context.registerClass(OperationResponse.class);
        context.registerClass(RequestBuilder.class);

        // Filter / repository / mapper.
        context.registerClass(Filter.class);
        context.registerClass(Repository.class);
        context.registerClass(DefaultMapper.class);

        // @Expression-annotated method holders — registered by garganttua-core's
        // ExpressionContextBuilder via IClass.getClass(...) when wiring its
        // resolvers into the InjectionContext. Each class hosts the expressions
        // for one slice of the framework (CRUD, security, serialisation, etc.).
        context.registerClass(ApiExpressions.class);
        context.registerClass(AuthorizationProtocolExpressions.class);
        context.registerClass(CrudExpressions.class);
        context.registerClass(EntityLifecycleExpressions.class);
        context.registerClass(ProtocolExpressions.class);
        context.registerClass(SecurityExpressions.class);
        context.registerClass(SerializationExpressions.class);
    }
}
