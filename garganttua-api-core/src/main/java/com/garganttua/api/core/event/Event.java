package com.garganttua.api.core.event;

import java.util.Date;
import java.util.Map;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.event.IEvent;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.service.OperationResponse;

/**
 * Concrete {@link IEvent} — the rich, business-level record of one domain
 * operation (operation, in/out payloads, caller, tenant/owner context,
 * timestamps, outcome code, failure info).
 * <p>
 * Built by {@code Domain.invoke()} at the operation boundary and carried as the
 * {@code payload} of the End/Error {@code ObservableEvent}, so any registered
 * {@code IEventPublisher} — bridged onto the observability stream by
 * {@link EventPublisherObserver} — receives it for downstream integration
 * (message brokers, mail, garganttua-events, …). The observability mechanism is
 * the <em>transport</em>; this is the <em>business payload</em> it carries.
 */
public class Event implements IEvent {

	private OperationDefinition operation;
	private Date inDate;
	private Date outDate;
	private int exceptionCode;
	private Map<String, String> inParams;
	private Object in;
	private Object out;
	private ICaller caller;
	private String tenantId;
	private String ownerId;
	private String userId;
	private String exceptionMessage;
	private OperationResponseCode code;

	@Override public OperationDefinition getOperation() { return this.operation; }
	@Override public void setOperation(OperationDefinition operation) { this.operation = operation; }

	@Override public Date getInDate() { return this.inDate; }
	@Override public void setInDate(Date inDate) { this.inDate = inDate; }

	@Override public Date getOutDate() { return this.outDate; }
	@Override public void setOutDate(Date outDate) { this.outDate = outDate; }

	@Override public int getExceptionCode() { return this.exceptionCode; }
	@Override public void setExceptionCode(int exceptionCode) { this.exceptionCode = exceptionCode; }

	@Override public Map<String, String> getInParams() { return this.inParams; }
	@Override public void setInParams(Map<String, String> inParams) { this.inParams = inParams; }

	@Override public Object getIn() { return this.in; }
	@Override public void setIn(Object in) { this.in = in; }

	@Override public Object getOut() { return this.out; }
	@Override public void setOut(Object out) { this.out = out; }

	@Override public ICaller getCaller() { return this.caller; }
	@Override public void setCaller(ICaller caller) { this.caller = caller; }

	@Override public String getTenantId() { return this.tenantId; }
	@Override public void setTenantId(String tenantId) { this.tenantId = tenantId; }

	@Override public String getOwnerId() { return this.ownerId; }
	@Override public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

	@Override public String getUserId() { return this.userId; }
	@Override public void setUserId(String userId) { this.userId = userId; }

	@Override public String getExceptionMessage() { return this.exceptionMessage; }
	@Override public void setExceptionMessage(String exceptionMessage) { this.exceptionMessage = exceptionMessage; }

	@Override public OperationResponseCode getCode() { return this.code; }
	@Override public void setCode(OperationResponseCode code) { this.code = code; }

	@Override
	public IOperationResponse toServiceResponse() {
		return new OperationResponse(this.code, this.out);
	}
}
