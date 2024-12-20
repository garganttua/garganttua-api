package com.garganttua.api.core.entity.methods;

import java.util.Map;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.entity.IGGAPIEntityDeleteMethod;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityDeleteMethod implements IGGAPIEntityDeleteMethod {

	private IGGAPIDomain domain;
	private IGGAPIRepository repository;
	private IGGObjectQuery objectQuery;
	private GGObjectAddress beforeDeleteMethodAddress;
	private GGObjectAddress afterDeleteMethodAddress;
	
	public GGAPIEntityDeleteMethod(IGGAPIDomain domain, IGGAPIRepository repository) throws GGAPIException {
		this.domain = domain;
		this.repository = repository;
		this.beforeDeleteMethodAddress = this.domain.getBeforeDeleteMethodAddress();
		this.afterDeleteMethodAddress = this.domain.getAfterDeleteMethodAddress();
		try {
			this.objectQuery = GGObjectQueryFactory.objectQuery(domain.getEntityClass());
		} catch (GGReflectionException e) {
			throw new GGAPIEntityException(e);
		}
	}

	@Override
	public  void delete(IGGAPICaller caller, Map<String, String> parameters, Object entity) throws GGAPIException {
		
		try {
			if( repository.doesExist(caller, entity) ) {
				if( this.beforeDeleteMethodAddress != null ) {
					this.objectQuery.invoke(entity, this.beforeDeleteMethodAddress, caller, parameters);
				}
				repository.delete(caller, entity);
				if( this.afterDeleteMethodAddress != null ) {
					this.objectQuery.invoke(entity, this.afterDeleteMethodAddress, caller, parameters);
				}
			} else {
				log.error("[domain ["+domain.getDomain()+"]] "+caller.toString()+" Error during entity deletion ");
				throw new GGAPIEntityException(GGAPIExceptionCode.DELETION_ERROR, "Error during entity "+GGAPIEntityHelper.getUuid(entity)+" deletion");
			}
		} catch (GGReflectionException e) {
			throw new GGAPIEntityException(GGAPIExceptionCode.DELETION_ERROR, "Error during entity "+GGAPIEntityHelper.getUuid(entity)+" deletion", e);
		}
	}

}
