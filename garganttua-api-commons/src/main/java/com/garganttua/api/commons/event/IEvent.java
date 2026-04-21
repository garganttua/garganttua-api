package com.garganttua.api.commons.event;

import java.util.Date;
import java.util.Map;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;

public interface IEvent {

	OperationDefinition getOperation();
    void setOperation(OperationDefinition operation);

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
