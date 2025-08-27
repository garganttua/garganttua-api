package com.garganttua.api.spec.interfasse;

import com.garganttua.api.spec.service.IGGAPIServiceInfos;

public interface IGGAPICustomizableInterface extends IGGAPIInterface {
  
  void addCustomService(IGGAPIServiceInfos service);

}
