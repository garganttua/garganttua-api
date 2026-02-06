package com.garganttua.api.core.legacy.entity.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import com.garganttua.api.core.legacy.caller.Caller;
import com.garganttua.api.core.legacy.engine.EngineException;
import com.garganttua.api.core.legacy.entity.exceptions.EntityException;
import com.garganttua.api.core.legacy.entity.tools.EntityHelper;
import com.garganttua.api.core.filter.Literal;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.entity.IEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.api.spec.factory.EntityIdentifier;
import com.garganttua.api.spec.factory.IFactory;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.updater.IEntityUpdater;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.query.IObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntitySaveMethod implements IEntitySaveMethod {

  private IDomain domain;
  private IRepository repository;
  private ObjectAddress afterUpdateMethodAddress;
  private ObjectAddress beforeUpdateMethodAddress;
  private ObjectAddress afterCreateMethodAddress;
  private ObjectAddress beforeCreateMethodAddress;
  private IEntityUpdater entityUpdater;
  private IFactory factory;

  public EntitySaveMethod(IDomain domain, IRepository repository, IFactory factory,
      IEntityUpdater updater) throws CoreException {
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
  public Object save(ICaller caller, Map<String, String> parameters, Object entity) throws CoreException {
    if (domain == null) {
      throw new EntityException(CoreExceptionCode.BAD_REQUEST, "Domain is null");
    }
    if (caller == null) {
      throw new EntityException(CoreExceptionCode.BAD_REQUEST, "Caller is null");
    }
    if (this.repository == null) {
      throw new EntityException(CoreExceptionCode.BAD_REQUEST, "Repository is null");
    }
    if (entity == null) {
      throw new EntityException(CoreExceptionCode.BAD_REQUEST, "Entity is null");
    }

    try {
      if (this.repository.doesExist(caller, entity)) {

        Object storedObject = this.factory.getEntityFromRepository(caller, new HashMap<String, String>(),
            EntityIdentifier.UUID, EntityHelper.getUuid(entity));
        Object updatedObject = this.entityUpdater.update(caller, storedObject, entity,
            this.domain.getAuthorizedUpdateFieldsAndAuthorizations());

        this.updateEntity(caller, parameters, updatedObject);

        return updatedObject;
      } else {

        this.createEntity(caller, parameters, entity);

        return entity;
      }
    } catch (ReflectionException e) {
      CoreException.processException(e);

      // Should never be reached
      return null;
    }
  }

  private void updateEntity(ICaller caller, Map<String, String> customParameters, Object entity)
      throws CoreException, ReflectionException {
    log.info("[domain [" + domain.getDomain() + "]] " + caller.toString()
        + " Updating entity with Uuid " + EntityHelper.getUuid(entity));
    IObjectQuery objectQuery = ObjectQueryFactory.objectQuery(entity);
    this.applyUpdateUnicityRule(domain, repository, caller, entity);
    if (this.beforeUpdateMethodAddress != null) {
      objectQuery.invoke(entity, this.beforeUpdateMethodAddress, caller, customParameters);
    }
    this.repository.save(caller, entity);
    if (this.afterUpdateMethodAddress != null) {
      objectQuery.invoke(entity, this.afterUpdateMethodAddress, caller, customParameters);
    }
  }

  private void applyUpdateUnicityRule(IDomain domain, IRepository repository, ICaller caller,
      Object entity) throws CoreException {
    if (domain.getUnicityFields() != null
        && domain.getUnicityFields().size() > 0) {
      List<Object> entities = this.checkUnicityFields(domain, repository, caller, entity,
          domain.getUnicityFields());
      if (entities.size() != 1
          && !EntityHelper.getUuid(entities.get(0)).equals(EntityHelper.getUuid(entity))) {
        log.warn("[domain [" + domain.getDomain() + "]] " + caller.toString()
            + " Entity with same unical fields already exists, fields "
            + domain.getUnicityFields());
        throw new EntityException(CoreExceptionCode.ENTITY_ALREADY_EXISTS,
            "Entity with same unical fields already exists, fields " + domain.getUnicityFields());
      }
    }
  }

  private void createEntity(ICaller caller, Map<String, String> customParameters, Object entity)
      throws CoreException, ReflectionException {
    IObjectQuery objectQuery = ObjectQueryFactory.objectQuery(entity);
    this.applyTenantEntityRule(domain, caller, entity);

    log.info("[domain [" + domain.getDomain() + "]] " + caller.toString()
        + " Creating entity with uuid {}", EntityHelper.getUuid(entity));

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

  private void applyCreationUnicityRule(IDomain domain, IRepository repository, ICaller caller,
      Object entity) throws CoreException {
    if (domain.getUnicityFields() != null
        && domain.getUnicityFields().size() > 0) {
      if (this.checkUnicityFields(domain, repository, caller, entity, domain.getUnicityFields())
          .size() > 0) {
        log.warn("[domain [" + domain.getDomain() + "]] " + caller.toString()
            + " Entity with same unical fields already exists, fields "
            + domain.getUnicityFields());
        throw new EntityException(CoreExceptionCode.ENTITY_ALREADY_EXISTS,
            "Entity with same unical fields already exists, fields " + domain.getUnicityFields());
      }
    }
  }

  private void applyOwnedEntityRule(IDomain domain, ICaller caller, Object entity) throws CoreException {
    if (domain.isOwnedEntity()) {
      if (caller.getOwnerId() != null && !caller.getOwnerId().isEmpty()) {
        try {
          String ownerId = caller.getOwnerId();

          if (ownerId.split(":").length != 2) {
            throw new EngineException(CoreExceptionCode.BAD_REQUEST,
                "Invalid ownerId [" + ownerId + "] should be of format DOMAIN:UUID");
          }

          ObjectQueryFactory.objectQuery(entity).setValue(domain.getOwnerIdFieldAddress(),
              ownerId);
        } catch (ReflectionException e) {
          CoreException.processException(e);

          // Should never be reached
          return;
        }
      } else {
        throw new EntityException(CoreExceptionCode.BAD_REQUEST, "No ownerId provided");
      }
    }
  }

  private void applyTenantEntityRule(IDomain domain, ICaller caller, Object entity) throws CoreException {
    if (domain.isTenantEntity()) {
      if ((caller.getRequestedTenantId() == null || caller.getRequestedTenantId().isEmpty())) {
        log.info("[domain [" + domain.getDomain() + "]] " + caller.toString()
            + " No uuid provided, generating one");
        if (EntityHelper.getUuid(entity) == null || ((String) EntityHelper.getUuid(entity)).isEmpty()) {
          EntityHelper.setUuid(entity, UUID.randomUUID().toString());
        }
//				((Caller) caller).setRequestedTenantId(EntityHelper.getUuid(entity));
      } else {
        EntityHelper.setUuid(entity, caller.getRequestedTenantId());
      }
    } else {
      if (EntityHelper.getUuid(entity) == null || EntityHelper.getUuid(entity).isEmpty()) {
        log.info("[domain [" + domain.getDomain() + "]] " + caller.toString()
            + " No uuid provided, generating one");
        EntityHelper.setUuid(entity, UUID.randomUUID().toString());
      }
    }
  }

  protected void checkMandatoryFields(List<ObjectAddress> mandatory, Object entity) throws CoreException {

    for (ObjectAddress field : mandatory) {
      try {

        IObjectQuery objectQuery = ObjectQueryFactory.objectQuery(entity);
        Object value = objectQuery.getValue(field);

        if (value == null) {
          throw new EntityException(CoreExceptionCode.BAD_REQUEST, "Field " + field + " is mandatory");
        } else if (value.toString().isEmpty()) {
          throw new EntityException(CoreExceptionCode.BAD_REQUEST, "Field " + field + " is mandatory");
        }
      } catch (IllegalArgumentException | ReflectionException e) {
        CoreException.processException(e);

        // Should never be reached
        return;
      }
    }
  }

  private List<Object> checkUnicityFields(IDomain domain, IRepository repository, ICaller caller,
      Object entity, List<Pair<ObjectAddress, UnicityScope>> unicity) throws CoreException {
    try {
      IObjectQuery objectQuery = ObjectQueryFactory.objectQuery(entity);

      List<ObjectAddress> systemScopeUnicities = unicity.stream().filter(u -> {
        return u.getValue1() == UnicityScope.system;
      }).map(u -> {
        return u.getValue0();
      }).collect(Collectors.toList());

      List<ObjectAddress> tenantScopeUnicities = unicity.stream().filter(u -> {
        return u.getValue1() == UnicityScope.tenant;
      }).map(u -> {
        return u.getValue0();
      }).collect(Collectors.toList());

      List<Object> tenantScopeResult = this.getUnicities(repository,
          Caller.createTenantCaller(caller.getRequestedTenantId()), tenantScopeUnicities, objectQuery);
      List<Object> systemScopeResult = this.getUnicities(repository, Caller.createSuperCaller(),
          systemScopeUnicities, objectQuery);
      
      return mergeListsWithoutDuplicates(tenantScopeResult, systemScopeResult);

    } catch (ReflectionException e) {
      log.error("[domain [" + domain.getDomain() + "]] " + caller.toString()
          + " Error during checking unicity fields for entity with Uuid " + EntityHelper.getUuid(entity), e);
      CoreException.processException(e);

      // Should never be reached
      return null;
    }
  }

  public static List<Object> mergeListsWithoutDuplicates(List<Object> list1, List<Object> list2) throws CoreException {
    Set<String> uuidSet = new HashSet<>();

    List<Object> mergedList = new ArrayList<>();

    mergeListWithUuidCheck(list1, uuidSet, mergedList);
    mergeListWithUuidCheck(list2, uuidSet, mergedList);

    return mergedList;
  }

  private static void mergeListWithUuidCheck(List<Object> list, Set<String> uuidSet, List<Object> resultList)
      throws CoreException {
    for (Object obj : list) {
      String uuid = EntityHelper.getUuid(obj);
      if (uuid != null && uuidSet.add(uuid)) {
        resultList.add(obj);
      }
    }
  }

  private List<Object> getUnicities(IRepository repository, ICaller caller, List<ObjectAddress> unicity,
      IObjectQuery objectQuery) throws ReflectionException, CoreException, EngineException {

	  if( unicity.size() == 0 ) {
		  return List.of();
	  }
	  
    List<String> values = new ArrayList<String>();
    for (ObjectAddress fieldName : unicity) {
      values.add(objectQuery.getValue(fieldName).toString());
    }
    String[] fieldValues = new String[values.size()];
    values.toArray(fieldValues);

    Literal literal = null;
    for (int i = 0; i < unicity.size(); i++) {
      Literal eqLiteral = Literal.eq(unicity.get(i).toString(), fieldValues[i]);
      if (literal == null) {
        literal = eqLiteral;
      } else {
        literal.orOperator(eqLiteral);
      }
    }

    return repository.getEntities(caller, null, literal, null);
  }
}
