package com.garganttua.api.spec.event;

import java.util.Date;
import java.util.Map;

import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;

public interface IEvent {

	Operation getOperation();
    void setOperation(Operation operation);

    Date getInDate();
    void setInDate(Date inDate);

    Date getOutDate();
    void setOutDate(Date outDate);

    int getExceptionCode();
    void setExceptionCode(int exceptionCode);

    Map<String, String> getInParams();
    void setInParams(Map<String, String> inParams);

    Object getIn();
    void setIn(Object in);

    Object getOut();
    void setOut(Object out);

    ICaller getCaller();
    void setCaller(ICaller caller);

    String getTenantId();
    void setTenantId(String tenantId);

    String getOwnerId();
    void setOwnerId(String ownerId);

    String getUserId();
    void setUserId(String userId);

    String getExceptionMessage();
    void setExceptionMessage(String exceptionMessage);

    OperationResponseCode getCode();
    void setCode(OperationResponseCode code);
    
    IOperationResponse toServiceResponse();
    
	/* IDomain getDomain();
	void setDomain(IDomain domain);
	 */
}
