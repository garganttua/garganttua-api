package com.garganttua.api.spec.interfasse;

import com.garganttua.api.spec.service.IServiceInfos;

public interface ICustomizableInterface extends IInterface {
  
  void addCustomService(IServiceInfos service);

}
