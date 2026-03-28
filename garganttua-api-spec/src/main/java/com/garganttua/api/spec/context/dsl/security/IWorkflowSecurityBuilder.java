package com.garganttua.api.spec.context.dsl.security;

import com.garganttua.api.spec.operation.Access;
import com.garganttua.api.spec.context.dsl.IDomainWorkflowBuilder;
import com.garganttua.api.spec.security.IWorkflowSecurity;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;

public interface IWorkflowSecurityBuilder<E>
		extends IAutomaticLinkedBuilder<IWorkflowSecurityBuilder<E>, IDomainWorkflowBuilder<E>, IWorkflowSecurity> {

	IWorkflowSecurityBuilder<E> access(Access access);

	IWorkflowSecurityBuilder<E> authority(boolean authority);

	IWorkflowSecurityBuilder<E> authority(String customAuthority);

	IWorkflowSecurityBuilder<E> disable(boolean disabled);

}
