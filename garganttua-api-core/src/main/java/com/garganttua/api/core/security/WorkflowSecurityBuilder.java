package com.garganttua.api.core.security;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.Objects;

import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.context.dsl.IDomainWorkflowBuilder;
import com.garganttua.api.commons.context.dsl.security.IWorkflowSecurityBuilder;
import com.garganttua.api.commons.security.IWorkflowSecurity;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.commons.ApiException;

@Reflected
public class WorkflowSecurityBuilder<E>
		extends AbstractAutomaticLinkedBuilder<IWorkflowSecurityBuilder<E>, IDomainWorkflowBuilder<E>, IWorkflowSecurity>
		implements IWorkflowSecurityBuilder<E> {

	private boolean disabled = false;
	private boolean authority = false;
	private String customAuthority;
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
		this.customAuthority = null;
		return this;
	}

	@Override
	public IWorkflowSecurityBuilder<E> authority(String customAuthority) {
		this.authority = true;
		this.customAuthority = Objects.requireNonNull(customAuthority, "Custom authority cannot be null");
		return this;
	}

	@Override
	public IWorkflowSecurityBuilder<E> disable(boolean disabled) {
		this.disabled = disabled;
		return this;
	}

	@Override
	protected synchronized IWorkflowSecurity doBuild() throws ApiException {
		return new WorkflowSecurityImpl(this.disabled, this.authority, this.customAuthority, this.access);
	}

	@Override
	protected void doAutoDetection() throws ApiException {
	}

	public boolean isDisabled() {
		return this.disabled;
	}

	public boolean hasAuthority() {
		return this.authority;
	}

	public String getCustomAuthority() {
		return this.customAuthority;
	}

	public Access getAccess() {
		return this.access;
	}

	private static class WorkflowSecurityImpl implements IWorkflowSecurity {

		private final boolean disabled;
		private final boolean authority;
		private final String customAuthority;
		private final Access access;

		public WorkflowSecurityImpl(boolean disabled, boolean authority, String customAuthority, Access access) {
			this.disabled = disabled;
			this.authority = authority;
			this.customAuthority = customAuthority;
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
		public String customAuthority() {
			return customAuthority;
		}

		@Override
		public Access getAccess() {
			return access;
		}
	}
}
