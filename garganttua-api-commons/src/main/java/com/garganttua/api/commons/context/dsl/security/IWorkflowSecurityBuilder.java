package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.context.dsl.IDomainWorkflowBuilder;
import com.garganttua.api.commons.security.IWorkflowSecurity;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;

public interface IWorkflowSecurityBuilder<E>
		extends IAutomaticLinkedBuilder<IWorkflowSecurityBuilder<E>, IDomainWorkflowBuilder<E>, IWorkflowSecurity> {

	IWorkflowSecurityBuilder<E> access(Access access);

	IWorkflowSecurityBuilder<E> authority(boolean authority);

	IWorkflowSecurityBuilder<E> authority(String customAuthority);

	IWorkflowSecurityBuilder<E> disable(boolean disabled);

}
