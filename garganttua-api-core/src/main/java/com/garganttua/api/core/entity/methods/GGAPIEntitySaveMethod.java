package com.garganttua.api.core.entity.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.entity.IGGAPIEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIUnicityScope;
import com.garganttua.api.spec.factory.GGAPIEntityIdentifier;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.updater.IGGAPIEntityUpdater;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntitySaveMethod implements IGGAPIEntitySaveMethod {

  private IGGAPIDomain domain;
  private IGGAPIRepository repository;
  private GGObjectAddress afterUpdateMethodAddress;
  private GGObjectAddress beforeUpdateMethodAddress;
  private GGObjectAddress afterCreateMethodAddress;
  private GGObjectAddress beforeCreateMethodAddress;
  private IGGAPIEntityUpdater<Object> entityUpdater;
  private IGGAPIEntityFactory<Object> factory;

  public GGAPIEntitySaveMethod(IGGAPIDomain domain, IGGAPIRepository repository, IGGAPIEntityFactory<Object> factory,
      IGGAPIEntityUpdater<Object> updater) throws GGAPIException {
    this.domain = domain;
    this.repository = repository;
    this.factory = factory;
    this.entityUpdater = updater;

    this.beforeCreateMethodAddress = this.domain.getBeforeCreateMethodAddress();
    this.afterCreateMethodAddress = this.domain.getAfterCreateMethodAddress();
    this.beforeUpdateMethodAddress = this.domain.getBeforeUpdateMethodAddress();
    this.afterUpdateMethodAddress = this.domain.getAfterUpdateMethodAddress();

  }

  @Override
  public Object save(IGGAPICaller caller, Map<String, String> parameters, Object entity) throws GGAPIException {
    if (domain == null) {
      throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Domain is null");
    }
    if (caller == null) {
      throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Caller is null");
    }
    if (this.repository == null) {
      throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Repository is null");
    }
    if (entity == null) {
      throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Entity is null");
    }

    try {
      if (this.repository.doesExist(caller, entity)) {

        Object storedObject = this.factory.getEntityFromRepository(caller, new HashMap<String, String>(),
            GGAPIEntityIdentifier.UUID, GGAPIEntityHelper.getUuid(entity));
        Object updatedObject = this.entityUpdater.update(caller, storedObject, entity,
            this.domain.getAuthorizedUpdateFieldsAndAuthorizations());

        this.updateEntity(caller, parameters, updatedObject);

        return updatedObject;
      } else {

        this.createEntity(caller, parameters, entity);

        return entity;
      }
    } catch (GGReflectionException e) {
      GGAPIException.processException(e);

      // Should never be reached
      return null;
    }
  }

  private void updateEntity(IGGAPICaller caller, Map<String, String> customParameters, Object entity)
      throws GGAPIException, GGReflectionException {
    log.info("[domain [" + domain.getDomain() + "]] " + caller.toString()
        + " Updating entity with Uuid " + GGAPIEntityHelper.getUuid(entity));
    IGGObjectQuery objectQuery = GGObjectQueryFactory.objectQuery(entity);
    this.applyUpdateUnicityRule(domain, repository, caller, entity);
    if (this.beforeUpdateMethodAddress != null) {
      objectQuery.invoke(entity, this.beforeUpdateMethodAddress, caller, customParameters);
    }
    this.repository.save(caller, entity);
    if (this.afterUpdateMethodAddress != null) {
      objectQuery.invoke(entity, this.afterUpdateMethodAddress, caller, customParameters);
    }
  }

  private void applyUpdateUnicityRule(IGGAPIDomain domain, IGGAPIRepository repository, IGGAPICaller caller,
      Object entity) throws GGAPIException {
    if (domain.getUnicityFields() != null
        && domain.getUnicityFields().size() > 0) {
      List<Object> entities = this.checkUnicityFields(domain, repository, caller, entity,
          domain.getUnicityFields());
      if (entities.size() != 1
          && !GGAPIEntityHelper.getUuid(entities.get(0)).equals(GGAPIEntityHelper.getUuid(entity))) {
        log.warn("[domain [" + domain.getDomain() + "]] " + caller.toString()
            + " Entity with same unical fields already exists, fields "
            + domain.getUnicityFields());
        throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_ALREADY_EXISTS,
            "Entity with same unical fields already exists, fields " + domain.getUnicityFields());
      }
    }
  }

  private void createEntity(IGGAPICaller caller, Map<String, String> customParameters, Object entity)
      throws GGAPIException, GGReflectionException {
    IGGObjectQuery objectQuery = GGObjectQueryFactory.objectQuery(entity);
    this.applyTenantEntityRule(domain, caller, entity);

    log.info("[domain [" + domain.getDomain() + "]] " + caller.toString()
        + " Creating entity with uuid {}", GGAPIEntityHelper.getUuid(entity));

    this.applyOwnedEntityRule(domain, caller, entity);

    if (this.domain.getMandatoryFields().size() > 0) {
      this.checkMandatoryFields(this.domain.getMandatoryFields(), entity);
    }

    this.applyCreationUnicityRule(domain, repository, caller, entity);
    if (this.beforeCreateMethodAddress != null) {
      objectQuery.invoke(entity, this.beforeCreateMethodAddress, caller, customParameters);
    }
    this.repository.save(caller, entity);
    if (this.afterCreateMethodAddress != null) {
      objectQuery.invoke(entity, this.afterCreateMethodAddress, caller, customParameters);
    }

  }

  private void applyCreationUnicityRule(IGGAPIDomain domain, IGGAPIRepository repository, IGGAPICaller caller,
      Object entity) throws GGAPIException {
    if (domain.getUnicityFields() != null
        && domain.getUnicityFields().size() > 0) {
      if (this.checkUnicityFields(domain, repository, caller, entity, domain.getUnicityFields())
          .size() > 0) {
        log.warn("[domain [" + domain.getDomain() + "]] " + caller.toString()
            + " Entity with same unical fields already exists, fields "
            + domain.getUnicityFields());
        throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_ALREADY_EXISTS,
            "Entity with same unical fields already exists, fields " + domain.getUnicityFields());
      }
    }
  }

  private void applyOwnedEntityRule(IGGAPIDomain domain, IGGAPICaller caller, Object entity) throws GGAPIException {
    if (domain.isOwnedEntity()) {
      if (caller.getOwnerId() != null && !caller.getOwnerId().isEmpty()) {
        try {
          String ownerId = caller.getOwnerId();

          if (ownerId.split(":").length != 2) {
            throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST,
                "Invalid ownerId [" + ownerId + "] should be of format DOMAIN:UUID");
          }

          GGObjectQueryFactory.objectQuery(entity).setValue(domain.getOwnerIdFieldAddress(),
              ownerId);
        } catch (GGReflectionException e) {
          GGAPIException.processException(e);

          // Should never be reached
          return;
        }
      } else {
        throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "No ownerId provided");
      }
    }
  }

  private void applyTenantEntityRule(IGGAPIDomain domain, IGGAPICaller caller, Object entity) throws GGAPIException {
    if (domain.isTenantEntity()) {
      if ((caller.getRequestedTenantId() == null || caller.getRequestedTenantId().isEmpty())) {
        log.info("[domain [" + domain.getDomain() + "]] " + caller.toString()
            + " No uuid provided, generating one");
        if (GGAPIEntityHelper.getUuid(entity) == null || ((String) GGAPIEntityHelper.getUuid(entity)).isEmpty()) {
          GGAPIEntityHelper.setUuid(entity, UUID.randomUUID().toString());
        }
//				((GGAPICaller) caller).setRequestedTenantId(GGAPIEntityHelper.getUuid(entity));
      } else {
        GGAPIEntityHelper.setUuid(entity, caller.getRequestedTenantId());
      }
    } else {
      if (GGAPIEntityHelper.getUuid(entity) == null || GGAPIEntityHelper.getUuid(entity).isEmpty()) {
        log.info("[domain [" + domain.getDomain() + "]] " + caller.toString()
            + " No uuid provided, generating one");
        GGAPIEntityHelper.setUuid(entity, UUID.randomUUID().toString());
      }
    }
  }

  protected void checkMandatoryFields(List<GGObjectAddress> mandatory, Object entity) throws GGAPIException {

    for (GGObjectAddress field : mandatory) {
      try {

        IGGObjectQuery objectQuery = GGObjectQueryFactory.objectQuery(entity);
        Object value = objectQuery.getValue(field);

        if (value == null) {
          throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Field " + field + " is mandatory");
        } else if (value.toString().isEmpty()) {
          throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Field " + field + " is mandatory");
        }
      } catch (IllegalArgumentException | GGReflectionException e) {
        GGAPIException.processException(e);

        // Should never be reached
        return;
      }
    }
  }

  private List<Object> checkUnicityFields(IGGAPIDomain domain, IGGAPIRepository repository, IGGAPICaller caller,
      Object entity, List<Pair<GGObjectAddress, GGAPIUnicityScope>> unicity) throws GGAPIException {
    try {
      IGGObjectQuery objectQuery = GGObjectQueryFactory.objectQuery(entity);

      List<GGObjectAddress> systemScopeUnicities = unicity.stream().filter(u -> {
        return u.getValue1() == GGAPIUnicityScope.system;
      }).map(u -> {
        return u.getValue0();
      }).collect(Collectors.toList());

      List<GGObjectAddress> tenantScopeUnicities = unicity.stream().filter(u -> {
        return u.getValue1() == GGAPIUnicityScope.tenant;
      }).map(u -> {
        return u.getValue0();
      }).collect(Collectors.toList());

      List<Object> tenantScopeResult = this.getUnicities(repository,
          GGAPICaller.createTenantCaller(caller.getRequestedTenantId()), tenantScopeUnicities, objectQuery);
      List<Object> systemScopeResult = this.getUnicities(repository, GGAPICaller.createSuperCaller(),
          systemScopeUnicities, objectQuery);
      
      return mergeListsWithoutDuplicates(tenantScopeResult, systemScopeResult);

    } catch (GGReflectionException e) {
      log.error("[domain [" + domain.getDomain() + "]] " + caller.toString()
          + " Error during checking unicity fields for entity with Uuid " + GGAPIEntityHelper.getUuid(entity), e);
      GGAPIException.processException(e);

      // Should never be reached
      return null;
    }
  }

  public static List<Object> mergeListsWithoutDuplicates(List<Object> list1, List<Object> list2) throws GGAPIException {
    Set<String> uuidSet = new HashSet<>();

    List<Object> mergedList = new ArrayList<>();

    mergeListWithUuidCheck(list1, uuidSet, mergedList);
    mergeListWithUuidCheck(list2, uuidSet, mergedList);

    return mergedList;
  }

  private static void mergeListWithUuidCheck(List<Object> list, Set<String> uuidSet, List<Object> resultList)
      throws GGAPIException {
    for (Object obj : list) {
      String uuid = GGAPIEntityHelper.getUuid(obj);
      if (uuid != null && uuidSet.add(uuid)) {
        resultList.add(obj);
      }
    }
  }

  private List<Object> getUnicities(IGGAPIRepository repository, IGGAPICaller caller, List<GGObjectAddress> unicity,
      IGGObjectQuery objectQuery) throws GGReflectionException, GGAPIException, GGAPIEngineException {

	  if( unicity.size() == 0 ) {
		  return List.of();
	  }
	  
    List<String> values = new ArrayList<String>();
    for (GGObjectAddress fieldName : unicity) {
      values.add(objectQuery.getValue(fieldName).toString());
    }
    String[] fieldValues = new String[values.size()];
    values.toArray(fieldValues);

    GGAPILiteral literal = null;
    for (int i = 0; i < unicity.size(); i++) {
      GGAPILiteral eqLiteral = GGAPILiteral.eq(unicity.get(i).toString(), fieldValues[i]);
      if (literal == null) {
        literal = eqLiteral;
      } else {
        literal.orOperator(eqLiteral);
      }
    }

    return repository.getEntities(caller, null, literal, null);
  }
}
