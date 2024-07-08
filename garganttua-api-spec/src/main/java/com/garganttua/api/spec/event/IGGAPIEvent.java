package com.garganttua.api.spec.event;

import java.util.Date;
import java.util.Map;

import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.service.GGAPIServiceMethod;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

public interface IGGAPIEvent {

	GGAPIServiceMethod getMethod();
    void setMethod(GGAPIServiceMethod method);

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

    IGGAPICaller getCaller();
    void setCaller(IGGAPICaller caller);

    String getTenantId();
    void setTenantId(String tenantId);

    String getOwnerId();
    void setOwnerId(String ownerId);

    String getUserId();
    void setUserId(String userId);

    String getExceptionMessage();
    void setExceptionMessage(String exceptionMessage);

    GGAPIServiceResponseCode getCode();
    void setCode(GGAPIServiceResponseCode code);
    
    IGGAPIServiceResponse toServiceResponse();
    
	IGGAPIDomain getDomain();
	void setDomain(IGGAPIDomain domain);
	
}
