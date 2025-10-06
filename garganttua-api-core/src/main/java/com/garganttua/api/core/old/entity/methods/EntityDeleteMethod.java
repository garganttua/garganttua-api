package com.garganttua.api.core.entity.methods;

import java.util.Map;

import com.garganttua.api.core.entity.exceptions.EntityException;
import com.garganttua.api.core.entity.tools.EntityHelper;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.entity.IEntityDeleteMethod;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityDeleteMethod implements IEntityDeleteMethod {

	private IDomain domain;
	private IRepository repository;
	private IGGObjectQuery objectQuery;
	private GGObjectAddress beforeDeleteMethodAddress;
	private GGObjectAddress afterDeleteMethodAddress;
	
	public EntityDeleteMethod(IDomain domain, IRepository repository) throws CoreException {
		this.domain = domain;
		this.repository = repository;
		this.beforeDeleteMethodAddress = this.domain.getBeforeDeleteMethodAddress();
		this.afterDeleteMethodAddress = this.domain.getAfterDeleteMethodAddress();
		try {
			this.objectQuery = GGObjectQueryFactory.objectQuery(domain.getEntityClass());
		} catch (GGReflectionException e) {
			throw new EntityException(e);
		}
	}

	@Override
	public  void delete(ICaller caller, Map<String, String> parameters, Object entity) throws CoreException {
		
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
				throw new EntityException(CoreExceptionCode.DELETION_ERROR, "Error during entity "+EntityHelper.getUuid(entity)+" deletion");
			}
		} catch (GGReflectionException e) {
			throw new EntityException(CoreExceptionCode.DELETION_ERROR, "Error during entity "+EntityHelper.getUuid(entity)+" deletion", e);
		}
	}

}
