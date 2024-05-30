package com.garganttua.api.core.entity.methods;

import java.util.Map;

import com.garganttua.api.core.engine.GGAPIDomain;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryFactory;
import com.garganttua.api.core.objects.query.IGGAPIObjectQuery;
import com.garganttua.api.core.repository.GGAPIRepositoryException;
import com.garganttua.api.spec.GGAPICoreExceptionCode;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.repository.IGGAPIRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityDeleteMethod implements IGGAPIEntityDeleteMethod<Object> {

	private GGAPIDomain domain;
	private IGGAPIRepository<Object> repository;
	private IGGAPIObjectQuery objectQuery;
	private GGAPIObjectAddress beforeDeleteMethodAddress;
	private GGAPIObjectAddress afterDeleteMethodAddress;
	
	public GGAPIEntityDeleteMethod(GGAPIDomain domain, IGGAPIRepository<Object> repository) throws GGAPIEntityException {
		this.domain = domain;
		this.repository = repository;
		this.beforeDeleteMethodAddress = this.domain.entity.getValue1().beforeDeleteMethodAddress();
		this.afterDeleteMethodAddress = this.domain.entity.getValue1().afterDeleteMethodAddress();
		try {
			this.objectQuery = GGAPIObjectQueryFactory.objectQuery(domain.entity.getValue0());
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		}
	}

	@Override
	public  void delete(IGGAPICaller caller, Map<String, String> parameters, Object entity) throws GGAPIEntityException {
		
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
				log.error("[domain ["+domain.entity.getValue1().domain()+"]] "+caller.toString()+" Error during entity deletion ");
				throw new GGAPIEntityException(GGAPICoreExceptionCode.DELETION_ERROR, "Error during entity "+GGAPIEntityHelper.getUuid(entity)+" deletion");
			}
		} catch (GGAPIEntityException | GGAPIRepositoryException | GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(GGAPICoreExceptionCode.DELETION_ERROR, "Error during entity "+GGAPIEntityHelper.getUuid(entity)+" deletion", e);
		}
	}

}
