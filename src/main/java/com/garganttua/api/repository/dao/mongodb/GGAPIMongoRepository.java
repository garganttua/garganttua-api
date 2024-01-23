package com.garganttua.api.repository.dao.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Shape;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.garganttua.api.core.GGAPIDomainable;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIMongoRepository<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends GGAPIDomainable<Entity, Dto> implements IGGAPIDAORepository<Entity, Dto> {

	protected MongoTemplate mongo;

	@Setter
	protected IGGAPIEngine engine;
	
	public void setMongoTemplate(MongoTemplate mongo) {
		this.mongo = mongo;
	}
	
	@Override
	public Dto save(Dto object) {
		return this.mongo.save(object);
	}
	
	@Override
	public List<Dto> find(Pageable pageable, GGAPILiteral filter, GGAPISort sort, GGAPIGeolocFilter geoloc) {
		
		List<Dto> results = new ArrayList<>();

		Query query = new Query();

		if (filter != null) {
			Criteria criteria = GGAPIMongoRepository.getCriteriaFromFilter(filter);
			query.addCriteria(criteria);
		}
		
		if (geoloc != null) {
			Criteria criteria = GGAPIMongoRepository.getCriteriaFromGeolocFilter(geoloc, this.dynamicDomain.geolocalized());
			if( criteria != null)
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
		if( filter != null ) {
			log.debug("		[domain ["+this.dynamicDomain.domain()+"]] Finding objects using filter "+filter.toString());
		}
		results = this.mongo.find(query, this.dtoClass);

		return results;
	}

	private static Criteria getCriteriaFromGeolocFilter(GGAPIGeolocFilter geoloc, String geolocField) {
		Criteria criteria = null;
		
		if( geolocField != null ) {
			Shape shape = geoloc.getShape().accept(new GGAPIGeoJsonToSpringShapeBinder());
			
			criteria = Criteria.where(geolocField);
			switch(geoloc.getType()) {
			default:
				break;
			case GEO_WITHIN:
				criteria.within(shape);
				break;
			case GEO_WITHIN_SPHERE:
				criteria.withinSphere((Circle) shape);
				break;
			}
		}
		
		return criteria;
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
	public void delete(Dto object) {
		this.mongo.remove(object);
	}

	@Override
	public long count(GGAPILiteral filter) {
		
		Query query = new Query();

		if (filter != null) {
			Criteria criteria = GGAPIMongoRepository.getCriteriaFromFilter(filter);
			query.addCriteria(criteria);
		}
		
		return this.mongo.count(query, this.dtoClass);
	}

	@Override
	public void setDomain(GGAPIDynamicDomain domain) {
		super.setDomain(domain);
		
		String geolocField = domain.geolocalized();
		if( geolocField != null && !geolocField.isEmpty())
			this.mongo.indexOps(this.domainObj.getDtoClass()).ensureIndex( new GeospatialIndex(geolocField).typed(GeoSpatialIndexType.GEO_2DSPHERE) );
	}
	
}
