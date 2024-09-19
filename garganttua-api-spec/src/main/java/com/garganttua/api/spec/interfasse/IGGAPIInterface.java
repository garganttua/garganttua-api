package com.garganttua.api.spec.interfasse;

import java.util.List;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngineObject;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

public interface IGGAPIInterface extends IGGAPIEngineObject{

	void start() throws GGAPIException;

	void setDomain(IGGAPIDomain domain);

	void setService(IGGAPIService service, List<IGGAPIServiceInfos> serviceInfos);

	String getName();

}
