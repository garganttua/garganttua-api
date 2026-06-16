package com.garganttua.api.core.expression;

import static com.garganttua.api.core.expression.ExpressionUtils.unwrapOptional;

import java.util.Optional;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IUseCaseDefinition;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.expression.annotations.Expression;
import com.garganttua.core.reflection.IMethodReturn;
import com.garganttua.core.reflection.annotations.Reflected;
import com.garganttua.core.reflection.binders.IContextualMethodBinder;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.runtime.RuntimeExpressionContext;

import jakarta.annotation.Nullable;

/**
 * Expressions backing the use-case business stage (mirrors {@code SecurityExpressions}): one to
 * discriminate the stage by use-case name, one to invoke the bound method.
 */
@Reflected(queryAllPublicMethods = true)
public class UseCaseExpressions {

	@Expression(name = "useCaseName",
			description = "Returns the use-case name carried by the request's operation (null for non-use-case ops) "
					+ "— so the assembler can guard one business stage per use case: "
					+ "and(equals(businessOperation(@0), \"useCase\"), equals(useCaseName(@0), \"<name>\")).")
	public static String useCaseName(@Nullable Object request) {
		if (!(unwrapOptional(request) instanceof IOperationRequest req)) {
			return null;
		}
		OperationDefinition op = req.operation();
		return op != null ? op.useCaseName() : null;
	}

	@Expression(name = "invokeUseCase",
			description = "Invokes the use case's bound method. Resolves the IMethodBinder from the operation's "
					+ "IUseCaseDefinition, seeds the runtime context (request / domainContext / apiContext) so the "
					+ "method's declared suppliers (@UseCaseInput, @Caller, @ApiContext, @Repository, …) resolve, and "
					+ "returns the method's single result — serialized as the response.")
	public static Object invokeUseCase(@Nullable Object request, @Nullable Object domainContextObj) {
		Object req = unwrapOptional(request);
		if (!(req instanceof IOperationRequest operationRequest)) {
			throw new ApiException("invokeUseCase: no IOperationRequest available");
		}
		OperationDefinition op = operationRequest.operation();
		IUseCaseDefinition useCase = op != null ? op.useCase() : null;
		if (useCase == null) {
			throw new ApiException("invokeUseCase: the operation carries no use-case definition");
		}
		IMethodBinder<?> binder = useCase.binder();
		if (binder == null) {
			throw new ApiException("Use case '" + useCase.name() + "' has no bound method — call .bind(...) on it");
		}

		IRuntimeContext<?, ?> runtimeCtx = RuntimeExpressionContext.get();
		if (runtimeCtx != null) {
			runtimeCtx.setVariable("request", operationRequest);
			Object domain = unwrapOptional(domainContextObj);
			if (domain instanceof IDomain<?> d) {
				runtimeCtx.setVariable("domainContext", d);
				IApi api = d.getApiContext();
				if (api != null) {
					runtimeCtx.setVariable("apiContext", api);
				}
			}
		}

		Optional<? extends IMethodReturn<?>> result;
		if (binder instanceof IContextualMethodBinder<?, ?> contextualBinder) {
			result = ((IContextualMethodBinder<?, Object>) contextualBinder).execute(runtimeCtx);
		} else {
			result = binder.execute();
		}
		return result.isPresent() ? result.get().single() : null;
	}
}
