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

import com.garganttua.api.repository.dao.ISpringCrudifyDAORepository;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.spec.ISpringCrudifyDomain;
import com.garganttua.api.spec.ISpringCrudifyEntity;
import com.garganttua.api.spec.SpringCrudifyDomainable;
import com.garganttua.api.spec.filter.SpringCrudifyLiteral;
import com.garganttua.api.spec.sort.SpringCrudifySort;

public class SpringCrudifyMongoRepository<Entity extends ISpringCrudifyEntity, Dto extends ISpringCrudifyDTOObject<Entity>> extends SpringCrudifyDomainable<Entity, Dto> implements ISpringCrudifyDAORepository<Entity, Dto> {

	public SpringCrudifyMongoRepository(ISpringCrudifyDomain<Entity, Dto> domain) {
		super(domain);
	}

	@Inject
	protected MongoTemplate mongo;
	
	@Value("${spring.domain.crudify.magicTenantId}")
	protected String magicTenantId;

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
	public List<Dto> findByTenantId(String tenantId, Pageable pageable, SpringCrudifyLiteral filter, SpringCrudifySort sort) {
		List<Dto> results = new ArrayList<>();

		Query query = new Query();
		
		if( !tenantId.equals(this.magicTenantId) ) {
			query.addCriteria(Criteria.where("tenantId").is(tenantId));
		}

		if (filter != null) {
			Criteria criteria = SpringCrudifyMongoRepository.getCriteriaFromFilter(filter);
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
	private static Criteria getCriteriaFromFilter(SpringCrudifyLiteral literal) {

		Criteria criteria = null;
		
		List<Criteria> criterias = new ArrayList<Criteria>();
		List<String> values = new ArrayList<String>();
		
		switch (literal.getName()) {
		case SpringCrudifyLiteral.OPERATOR_OR:
			for( SpringCrudifyLiteral subliteral: literal.getLiterals() ) {
				criterias.add(getCriteriaFromFilter(subliteral));
			}
			criteria = new Criteria().orOperator(criterias);
			break;
		case SpringCrudifyLiteral.OPERATOR_AND:
			for( SpringCrudifyLiteral subliteral: literal.getLiterals() ) {
				criterias.add(getCriteriaFromFilter(subliteral));
			}
			criteria = new Criteria().andOperator(criterias);
			break;
		case SpringCrudifyLiteral.OPERATOR_NOR:
			for( SpringCrudifyLiteral subliteral: literal.getLiterals() ) {
				criterias.add(getCriteriaFromFilter(subliteral));
			}
			criteria = new Criteria().norOperator(criterias);
			break;
		case SpringCrudifyLiteral.OPERATOR_FIELD:
			
			SpringCrudifyLiteral subLiteral = literal.getLiterals().get(0);
			
			switch(subLiteral.getName()) {
			case SpringCrudifyLiteral.OPERATOR_EQUAL:
				criteria = Criteria.where(literal.getValue()).is(subLiteral.getValue());
				break;
			case SpringCrudifyLiteral.OPERATOR_NOT_EQUAL:
				criteria = Criteria.where(literal.getValue()).ne(subLiteral.getValue());
				break;
			case SpringCrudifyLiteral.OPERATOR_GREATER_THAN:
				criteria = Criteria.where(literal.getValue()).gt(subLiteral.getValue());
				break;
			case SpringCrudifyLiteral.OPERATOR_GREATER_THAN_EXCLUSIVE:
				criteria = Criteria.where(literal.getValue()).gte(subLiteral.getValue());
				break;
			case SpringCrudifyLiteral.OPERATOR_LOWER_THAN:
				criteria = Criteria.where(literal.getValue()).lt(subLiteral.getValue());
				break;
			case SpringCrudifyLiteral.OPERATOR_LOWER_THAN_EXCLUSIVE:
				criteria = Criteria.where(literal.getValue()).lte(subLiteral.getValue());
				break;
			case SpringCrudifyLiteral.OPERATOR_REGEX:
				criteria = Criteria.where(literal.getValue()).regex(subLiteral.getValue());
				break;
			case SpringCrudifyLiteral.OPERATOR_IN:
				for( SpringCrudifyLiteral subliteral: literal.getLiterals() ) {
					values.add(subliteral.getValue());
				}
				criteria = Criteria.where(literal.getValue()).in(values);
				break;
			case SpringCrudifyLiteral.OPERATOR_NOT_IN:
				for( SpringCrudifyLiteral subliteral: literal.getLiterals() ) {
					values.add(subliteral.getValue());
				}
				criteria = Criteria.where(literal.getValue()).nin(values);
				break;
			case SpringCrudifyLiteral.OPERATOR_EMPTY:
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
		
		if( !tenantId.equals(this.magicTenantId) ) {
			query.addCriteria(Criteria.where("tenantId").is(tenantId).and("uuid").is(uuid));
		} else {
			query.addCriteria(Criteria.where("uuid").is(uuid));
		}
		
		return this.mongo.findOne(query, this.dtoClass);
	}

	@Override
	public Dto findOneByIdAndTenantId(String id, String tenantId) {
		
		Query query = new Query();
		
		if( !tenantId.equals(this.magicTenantId) ) {
			query.addCriteria(Criteria.where("tenantId").is(tenantId).and("id").is(id));
		} else {
			query.addCriteria(Criteria.where("id").is(id));
		}
		
		return this.mongo.findOne(query, this.dtoClass);
	}

	@Override
	public void delete(Dto object) {

		this.mongo.remove(object);
	}

	@Override
	public long countByTenantId(String tenantId, SpringCrudifyLiteral filter) {
		
		Query query = new Query();
		
		if( !tenantId.equals(this.magicTenantId) ) {
			query.addCriteria(Criteria.where("tenantId").is(tenantId));
		}

		if (filter != null) {
			Criteria criteria = SpringCrudifyMongoRepository.getCriteriaFromFilter(filter);
			query.addCriteria(criteria);
		}
		
		return this.mongo.count(query, this.dtoClass);
	}

}
