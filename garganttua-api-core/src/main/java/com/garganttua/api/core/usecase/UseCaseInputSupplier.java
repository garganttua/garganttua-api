package com.garganttua.api.core.usecase;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.observability.Logger;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

/**
 * Supplies a use case's bound method with the deserialized request body (its {@code InputType}).
 * Reads the {@code request} runtime variable (seeded by {@code invokeUseCase}) and returns the body
 * the deserialize stage placed under {@code entity} / {@code body}. Mirrors {@code CallerSupplier}.
 */
@SuppressWarnings("rawtypes")
public class UseCaseInputSupplier implements IContextualSupplier<Object, IRuntimeContext> {

	private static final Logger log = Logger.getLogger(UseCaseInputSupplier.class);
	private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

	private final IClass<?> inputType;

	public UseCaseInputSupplier(IClass<?> inputType) {
		this.inputType = inputType != null ? inputType : IClass.getClass(Object.class);
	}

	@Override
	public Type getSuppliedType() {
		return this.inputType.getType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IClass<Object> getSuppliedClass() {
		return (IClass<Object>) this.inputType;
	}

	@Override
	public IClass<IRuntimeContext> getOwnerContextType() {
		return CONTEXT_CLASS;
	}

	@Override
	public Optional<Object> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
		if (context == null) {
			throw new SupplyException("IRuntimeContext cannot be null");
		}
		Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
		if (requestOpt.isEmpty()) {
			throw new SupplyException("Variable 'request' not found in runtime context");
		}
		IOperationRequest request = (IOperationRequest) requestOpt.get();
		Object input = request.arg("entity").orElse(null);
		if (input == null) {
			input = request.arg(IOperationRequest.BODY).orElse(null);
		}
		log.debug("UseCaseInputSupplier resolved input (present={})", input != null);
		return Optional.ofNullable(input);
	}
}
