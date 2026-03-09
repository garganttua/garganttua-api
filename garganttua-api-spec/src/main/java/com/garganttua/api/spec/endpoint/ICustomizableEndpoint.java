package com.garganttua.api.spec.endpoint;

import com.garganttua.api.spec.service.IServiceInfos;

public interface ICustomizableEndpoint extends IEndpoint {
  
  void addCustomService(IServiceInfos service);

}
