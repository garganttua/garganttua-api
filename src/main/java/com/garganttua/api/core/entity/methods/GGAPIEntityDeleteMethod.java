package com.garganttua.api.core.entity.methods;

import java.util.Map;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterDelete;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeDelete;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.repository.IGGAPIRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityDeleteMethod implements IGGAPIEntityDeleteMethod {

	@Override
	public <Entity extends IGGAPIEntity> void delete(GGAPIDynamicDomain domain, IGGAPIRepository repository, IGGAPICaller caller, Map<String, String> parameters, Entity entity) throws GGAPIEntityException, GGAPIEngineException {
		
		if( repository.doesExist(caller, entity) ) {
			GGAPIBusinessAnnotations.hasAnnotationAndInvoke(entity.getClass(), GGAPIEntityBeforeDelete.class, entity, caller, parameters);
			repository.delete(caller, entity);
			GGAPIBusinessAnnotations.hasAnnotationAndInvoke(entity.getClass(), GGAPIEntityAfterDelete.class, entity, caller, parameters);
		} else {
			log.error("[domain ["+domain.domain+"]] "+caller.toString()+" Error during entity deletion ");
			throw new GGAPIEntityException(GGAPIEntityException.UNKNOWN_ERROR, "Error during entities deletion");
		}
	}

}