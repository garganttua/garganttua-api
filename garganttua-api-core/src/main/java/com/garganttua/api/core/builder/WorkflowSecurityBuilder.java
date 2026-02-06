package com.garganttua.api.core.builder;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.dsl.IDomainWorkflowBuilder;
import com.garganttua.api.spec.context.dsl.security.IWorkflowSecurityBuilder;
import com.garganttua.api.spec.security.IWorkflowSecurity;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.dsl.DslException;

public class WorkflowSecurityBuilder<E>
		extends AbstractAutomaticLinkedBuilder<IWorkflowSecurityBuilder<E>, IDomainWorkflowBuilder<E>, IWorkflowSecurity>
		implements IWorkflowSecurityBuilder<E> {

	private boolean disabled = false;
	private boolean authority = false;
	private Access access = Access.authenticated;

	public WorkflowSecurityBuilder(IDomainWorkflowBuilder<E> up) {
		super(up);
	}

	@Override
	public IWorkflowSecurityBuilder<E> access(Access access) {
		this.access = access;
		return this;
	}

	@Override
	public IWorkflowSecurityBuilder<E> authority(boolean authority) {
		this.authority = authority;
		return this;
	}

	@Override
	public IWorkflowSecurityBuilder<E> disable(boolean disabled) {
		this.disabled = disabled;
		return this;
	}

	@Override
	protected synchronized IWorkflowSecurity doBuild() throws DslException {
		return new WorkflowSecurityImpl(this.disabled, this.authority, this.access);
	}

	@Override
	protected void doAutoDetection() throws DslException {
	}

	boolean isDisabled() {
		return this.disabled;
	}

	boolean hasAuthority() {
		return this.authority;
	}

	Access getAccess() {
		return this.access;
	}

	private static class WorkflowSecurityImpl implements IWorkflowSecurity {

		private final boolean disabled;
		private final boolean authority;
		private final Access access;

		public WorkflowSecurityImpl(boolean disabled, boolean authority, Access access) {
			this.disabled = disabled;
			this.authority = authority;
			this.access = access;
		}

		@Override
		public boolean isDisabled() {
			return disabled;
		}

		@Override
		public boolean hasAuthority() {
			return authority;
		}

		@Override
		public Access getAccess() {
			return access;
		}
	}
}
