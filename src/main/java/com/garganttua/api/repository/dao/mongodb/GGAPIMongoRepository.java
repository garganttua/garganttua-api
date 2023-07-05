package com.garganttua.api.repository.dao.mongodb;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.GGAPIDomainable;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.sort.GGAPISort;

public class GGAPIMongoRepository<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends GGAPIDomainable<Entity, Dto> implements IGGAPIDAORepository<Entity, Dto> {

	public GGAPIMongoRepository(IGGAPIDomain<Entity, Dto> domain) {
		super(domain);
	}

	@Inject
	protected MongoTemplate mongo;
	
	@Value("${com.garganttua.api.magicTenantId}")
	protected String magicTenantId;

	private boolean hiddenable;

	private boolean publicEntity;

	private String field;

	@Override
	public void setMagicTenantId(String magicTenantId) {
		this.magicTenantId = magicTenantId;
	}
	
	public void setMongoTemplate(MongoTemplate mongo) {
		this.mongo = mongo;
	}
	
	@Override
	public Dto save(Dto object) {
		return this.mongo.save(object);
	}
	
	@Override
	public List<Dto> findByTenantId(String tenantId, Pageable pageable, GGAPILiteral filter, GGAPISort sort) {
		List<Dto> results = new ArrayList<>();

		Query query = new Query();
		
		if( this.publicEntity ) {
			if( this.hiddenable ) {
				query.addCriteria(Criteria.where("visible").is(true));
			} 
		} else {
			if( !tenantId.equals(this.magicTenantId) ) {
				query.addCriteria(Criteria.where("tenantId").is(tenantId));
			}
	
			if( this.field != null && !this.field.isEmpty() ) {
				if( this.hiddenable ) {
					query.addCriteria(Criteria.where(field).is(tenantId).and("visible").is(true));
				} else {
					query.addCriteria(Criteria.where(field).is(tenantId));
				}
			}
		}

		if (filter != null) {
			Criteria criteria = GGAPIMongoRepository.getCriteriaFromFilter(filter);
			query.addCriteria(criteria);
		}

		if (pageable != null) {
			query.with(pageable);
		}
		
		if( sort != null ) {
			Direction direction = null;
			switch(sort.getDirection()) {
			case asc -> {direction = Sort.Direction.ASC;}
			case desc -> {direction = Sort.Direction.DESC;}
			}
			query.with(Sort.by(direction, sort.getFieldName()));
		}
		
		results = this.mongo.find(query, this.dtoClass);

		return results;
	}

	/*
	 * Not very elegant, must be refactored
	 */
	private static Criteria getCriteriaFromFilter(GGAPILiteral literal) {

		Criteria criteria = null;
		
		List<Criteria> criterias = new ArrayList<Criteria>();
		List<String> values = new ArrayList<String>();
		
		switch (literal.getName()) {
		case GGAPILiteral.OPERATOR_OR:
			for( GGAPILiteral subliteral: literal.getLiterals() ) {
				criterias.add(getCriteriaFromFilter(subliteral));
			}
			criteria = new Criteria().orOperator(criterias);
			break;
		case GGAPILiteral.OPERATOR_AND:
			for( GGAPILiteral subliteral: literal.getLiterals() ) {
				criterias.add(getCriteriaFromFilter(subliteral));
			}
			criteria = new Criteria().andOperator(criterias);
			break;
		case GGAPILiteral.OPERATOR_NOR:
			for( GGAPILiteral subliteral: literal.getLiterals() ) {
				criterias.add(getCriteriaFromFilter(subliteral));
			}
			criteria = new Criteria().norOperator(criterias);
			break;
		case GGAPILiteral.OPERATOR_FIELD:
			
			GGAPILiteral subLiteral = literal.getLiterals().get(0);
			
			switch(subLiteral.getName()) {
			case GGAPILiteral.OPERATOR_EQUAL:
				criteria = Criteria.where(literal.getValue()).is(subLiteral.getValue());
				break;
			case GGAPILiteral.OPERATOR_NOT_EQUAL:
				criteria = Criteria.where(literal.getValue()).ne(subLiteral.getValue());
				break;
			case GGAPILiteral.OPERATOR_GREATER_THAN:
				criteria = Criteria.where(literal.getValue()).gt(subLiteral.getValue());
				break;
			case GGAPILiteral.OPERATOR_GREATER_THAN_EXCLUSIVE:
				criteria = Criteria.where(literal.getValue()).gte(subLiteral.getValue());
				break;
			case GGAPILiteral.OPERATOR_LOWER_THAN:
				criteria = Criteria.where(literal.getValue()).lt(subLiteral.getValue());
				break;
			case GGAPILiteral.OPERATOR_LOWER_THAN_EXCLUSIVE:
				criteria = Criteria.where(literal.getValue()).lte(subLiteral.getValue());
				break;
			case GGAPILiteral.OPERATOR_REGEX:
				criteria = Criteria.where(literal.getValue()).regex(subLiteral.getValue());
				break;
			case GGAPILiteral.OPERATOR_IN:
				for( GGAPILiteral subliteral: literal.getLiterals() ) {
					values.add(subliteral.getValue());
				}
				criteria = Criteria.where(literal.getValue()).in(values);
				break;
			case GGAPILiteral.OPERATOR_NOT_IN:
				for( GGAPILiteral subliteral: literal.getLiterals() ) {
					values.add(subliteral.getValue());
				}
				criteria = Criteria.where(literal.getValue()).nin(values);
				break;
			case GGAPILiteral.OPERATOR_EMPTY:
				criteria = Criteria.where(literal.getValue()).isNullValue();
				break;
			}
			break;
		}

		return criteria;
	}

	@Override
	public Dto findOneByUuidAndTenantId(String uuid, String tenantId) {
	
		Query query = new Query();
		
		if( this.publicEntity ) {
			if( this.hiddenable ) {
				query.addCriteria(Criteria.where("visible").is(true));
			} 
		} else {
			if( !tenantId.equals(this.magicTenantId) ) {
				query.addCriteria(Criteria.where("tenantId").is(tenantId).and("uuid").is(uuid));
			} else {
				query.addCriteria(Criteria.where("uuid").is(uuid));
			}
			
			if( this.field != null && !this.field.isEmpty() ) {
				if( this.hiddenable ) {
					query.addCriteria(Criteria.where(field).is(tenantId).and("visible").is(true));
				} else {
					query.addCriteria(Criteria.where(field).is(tenantId));
				}
			}
		}
		
		return this.mongo.findOne(query, this.dtoClass);
	}

	@Override
	public Dto findOneByIdAndTenantId(String id, String tenantId) {
		
		Query query = new Query();
		
		if( this.publicEntity ) {
			if( this.hiddenable ) {
				query.addCriteria(Criteria.where("visible").is(true));
			} 
		} else {
			if( !tenantId.equals(this.magicTenantId) ) {
				query.addCriteria(Criteria.where("tenantId").is(tenantId).and("id").is(id));
			} else {
				query.addCriteria(Criteria.where("id").is(id));
			}
			
			if( this.field != null && !this.field.isEmpty() ) {
				if( this.hiddenable ) {
					query.addCriteria(Criteria.where(field).is(tenantId).and("visible").is(true));
				} else {
					query.addCriteria(Criteria.where(field).is(tenantId));
				}
			}
		}
		
		
		return this.mongo.findOne(query, this.dtoClass);
	}

	@Override
	public void delete(Dto object) {

		this.mongo.remove(object);
	}

	@Override
	public long countByTenantId(String tenantId, GGAPILiteral filter) {
		
		Query query = new Query();
		
		if( this.publicEntity ) {
			if( this.hiddenable ) {
				query.addCriteria(Criteria.where("visible").is(true));
			} 
		} else {
			if( !tenantId.equals(this.magicTenantId) ) {
				query.addCriteria(Criteria.where("tenantId").is(tenantId));
			}
			
			if( this.field != null && !this.field.isEmpty() ) {
				if( this.hiddenable ) {
					query.addCriteria(Criteria.where(field).is(tenantId).and("visible").is(true));
				} else {
					query.addCriteria(Criteria.where(field).is(tenantId));
				}
			}
		}
	
		if (filter != null) {
			Criteria criteria = GGAPIMongoRepository.getCriteriaFromFilter(filter);
			query.addCriteria(criteria);
		}
		
		return this.mongo.count(query, this.dtoClass);
	}

	@Override
	public void setHiddenable(boolean hiddenable) {
		this.hiddenable = hiddenable;
	}

	@Override
	public void setPublic(boolean publicEntity) {
		this.publicEntity = publicEntity;
	}

	@Override
	public void setShared(String field) {
		this.field = field;	
	}

}
