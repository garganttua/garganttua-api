package com.garganttua.api.core.entity.methods;

import java.util.Map;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterDelete;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeDelete;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.engine.GGAPIDomain;
import com.garganttua.api.repository.GGAPIRepositoryException;
import com.garganttua.api.repository.IGGAPIRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class GGAPIEntityDeleteMethod implements IGGAPIEntityDeleteMethod<Object> {

	private GGAPIDomain domain;
	private IGGAPIRepository<Object> repository;
	
	@Override
	public  void delete(IGGAPICaller caller, Map<String, String> parameters, Object entity) throws GGAPIEntityException {
		
		try {
			if( repository.doesExist(caller, entity) ) {
				GGAPIBusinessAnnotations.hasAnnotationAndInvoke(entity.getClass(), GGAPIEntityBeforeDelete.class, entity, caller, parameters);
				repository.delete(caller, entity);
				GGAPIBusinessAnnotations.hasAnnotationAndInvoke(entity.getClass(), GGAPIEntityAfterDelete.class, entity, caller, parameters);
			} else {
				log.error("[domain ["+domain.entity.getValue1().domain()+"]] "+caller.toString()+" Error during entity deletion ");
				throw new GGAPIEntityException(GGAPIEntityException.DELETION_ERROR, "Error during entity "+GGAPIEntityHelper.getUuid(entity)+" deletion");
			}
		} catch (GGAPIEntityException | GGAPIRepositoryException e) {
			throw new GGAPIEntityException(GGAPIEntityException.DELETION_ERROR, "Error during entity "+GGAPIEntityHelper.getUuid(entity)+" deletion", e);
		}
	}

}
