package com.garganttua.api.commons.definition;

import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.OperationPath;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.binders.IMethodBinder;

/**
 * The full specification of a domain use case — the use-case counterpart of an entity/workflow
 * definition. Carried by the use case's {@code OperationDefinition} (so {@code getPath()} /
 * {@code resolveBodyType()} / identity read from it), and reached by the workflow assembler and the
 * transport to route + execute the bound method.
 */
public interface IUseCaseDefinition {

	/** Unique name within the domain — disambiguates use cases (a domain may host several). */
	String name();

	/** The HTTP route path (from {@code pathSuffix} / {@code completePath}; carries {@code ${uuid}} when oneEntity). */
	OperationPath path();

	/** The deserialized request-body type handed to the bound method (via {@code @UseCaseInput}); may be {@code null}. */
	IClass<?> inputType();

	/** The type the bound method returns, serialized as the response; may be {@code null}. */
	IClass<?> outputType();

	/** The bound method to invoke — the use case's executable, fed by suppliers the method declares. */
	IMethodBinder<?> binder();

	Scope scope();

	/** The HTTP verb, expressed as a {@link TechnicalOperation} (read→GET, create→POST, update→PUT, delete→DELETE). */
	TechnicalOperation operation();

	Access access();

	boolean authority();

	/**
	 * Custom authority name configured on the use case via
	 * {@code useCase().security().authority(String)}, or {@code null} when no
	 * explicit name was provided.
	 */
	String authorityName();
}
