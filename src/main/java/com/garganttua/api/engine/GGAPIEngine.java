package com.garganttua.api.engine;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import com.garganttua.api.business.IGGAPIBusiness;
import com.garganttua.api.connector.IGGAPIConnector;
import com.garganttua.api.controller.GGAPIEngineController;
import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.events.IGGAPIEventPublisher;
import com.garganttua.api.repository.GGAPIEngineRepository;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.repository.dao.GGAPIDao;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dao.mongodb.GGAPIEngineMongoRepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.repository.dto.IGGAPIHiddenableDTO;
import com.garganttua.api.security.authorization.BasicGGAPIAuthorization;
import com.garganttua.api.spec.GGAPICrudAccess;
import com.garganttua.api.spec.GGAPICrudOperation;
import com.garganttua.api.spec.GGAPIEntity;
import com.garganttua.api.spec.GGAPIEntityHelper;
import com.garganttua.api.spec.GGAPIReadOutputMode;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.IGGAPIEntityWithTenant;
import com.garganttua.api.spec.IGGAPIHiddenableEntity;
import com.garganttua.api.ws.GGAPIEngineRestService;
import com.garganttua.api.ws.IGGAPIRestService;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class GGAPIEngine implements IGGAPIEngine {

	@Autowired
	protected Optional<MongoTemplate> mongo;

	@Autowired
	protected ApplicationContext context;

	@Value("${com.garganttua.api.magicTenantId}")
	protected String magicTenantId;

	@Value("${com.garganttua.api.engine.packages}")
	protected String[] scanPackages;

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Autowired
	public Optional<OpenAPI> openApi;

	private List<IGGAPIRestService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> services;

	private Map<String, IGGAPIDAORepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> daos = new HashMap<String, IGGAPIDAORepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>>();
	private Map<String, IGGAPIRepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> repositries = new HashMap<String, IGGAPIRepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>>();
	private Map<String, IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> controllers = new HashMap<String, IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>>();
	private Map<String, IGGAPIRestService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> restServices = new HashMap<String, IGGAPIRestService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>>();

	private GGAPIOpenAPIHelper openApiHelper;

	@Override
	public IGGAPIDAORepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getDao(
			String name) {
		return this.daos.get(name);
	}

	@Override
	public IGGAPIRepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getRepository(
			String name) {
		return this.repositries.get(name);
	}

	@Override
	public IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getController(
			String name) {
		return this.controllers.get(name);
	}

	@Override
	public IGGAPIRestService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getService(
			String name) {
		return this.restServices.get(name);
	}

	@SuppressWarnings({ "unchecked" })
	@Bean
	@Qualifier(value = "dynamicServices")
	protected List<IGGAPIRestService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> engineServices()
			throws GGAPIEngineException {

		this.openApiHelper = new GGAPIOpenAPIHelper();

		log.info("============================================");
		log.info("======                                ======");
		log.info("====== Starting Garganttua API Engine ======");
		log.info("======                                ======");
		log.info("============================================");
		log.info("Version: {}", this.getClass().getPackage().getImplementationVersion());

		this.services = new ArrayList<IGGAPIRestService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>>();
		boolean tenantFound = false;

		for (String pack : this.scanPackages) {
			log.info("Scanning package " + pack);

			Reflections reflections = new Reflections(pack);

			Set<Class<?>> entities__ = reflections.getTypesAnnotatedWith(GGAPIEntity.class);

			for (Class<?> clazz : entities__) {

				if (!IGGAPIEntity.class.isAssignableFrom(clazz)) {
					throw new GGAPIEngineException(
							"The class [" + clazz.getName() + "] must implements the IGGAPIEntity interface.");
				}

				Class<IGGAPIEntity> entityClass = (Class<IGGAPIEntity>) clazz;
				Class<IGGAPIDTOObject<IGGAPIEntity>> dtoClass = null;

				GGAPIEntity entityAnnotation = clazz.getAnnotation(GGAPIEntity.class);

				boolean allow_creation = entityAnnotation.allow_creation();
				boolean allow_read_all = entityAnnotation.allow_read_all();
				boolean allow_read_one = entityAnnotation.allow_read_one();
				boolean allow_update_one = entityAnnotation.allow_update_one();
				boolean allow_delete_one = entityAnnotation.allow_delete_one();
				boolean allow_delete_all = entityAnnotation.allow_delete_all();
				boolean allow_count = entityAnnotation.allow_count();

				GGAPICrudAccess creation_access = entityAnnotation.creation_access();
				GGAPICrudAccess read_all_access = entityAnnotation.read_all_access();
				GGAPICrudAccess read_one_access = entityAnnotation.read_one_access();
				GGAPICrudAccess update_one_access = entityAnnotation.update_one_access();
				GGAPICrudAccess delete_one_access = entityAnnotation.delete_one_access();
				GGAPICrudAccess delete_all_access = entityAnnotation.delete_all_access();
				GGAPICrudAccess count_access = entityAnnotation.count_access();

				boolean creation_authority = entityAnnotation.creation_authority();
				boolean read_all_authority = entityAnnotation.read_all_authority();
				boolean read_one_authority = entityAnnotation.read_one_authority();
				boolean update_one_authority = entityAnnotation.update_one_authority();
				boolean delete_one_authority = entityAnnotation.delete_one_authority();
				boolean delete_all_authority = entityAnnotation.delete_all_authority();
				boolean count_authority = entityAnnotation.count_authority();

				boolean hiddenable = entityAnnotation.hiddenAble();
				boolean publicEntity = entityAnnotation.publicEntity();
				String shared = entityAnnotation.shared();

				boolean tenant = entityAnnotation.tenantEntity();
				String[] unicity = entityAnnotation.unicity();
				
				boolean showTenantId = entityAnnotation.showTenantId();

				if (tenant && !tenantFound) {
					tenantFound = true;
				} else if (tenant && !tenantFound) {
					throw new GGAPIEngineException("There are more than one entity declared as tenantEntity.");
				}

				String domain = GGAPIEntityHelper.getDomain(entityClass);

				try {
					dtoClass = (Class<IGGAPIDTOObject<IGGAPIEntity>>) Class.forName(entityAnnotation.dto());
				} catch (ClassNotFoundException e) {
					throw new GGAPIEngineException(e);
				}

				if (hiddenable) {
					if (!IGGAPIHiddenableEntity.class.isAssignableFrom(clazz)) {
						throw new GGAPIEngineException("The class [" + clazz.getName()
								+ "] must implements the IGGAPIHiddenableEntity interface as it is mentionned as 'hiddenable'.");
					}

					if (!IGGAPIHiddenableDTO.class.isAssignableFrom(dtoClass)) {
						throw new GGAPIEngineException("The class [" + clazz.getName()
								+ "] must implements the IGGAPIHiddenableDTO interface as it is mentionned as 'hiddenable'.");
					}
				}
				
				if(showTenantId) {
					if (!IGGAPIEntityWithTenant.class.isAssignableFrom(clazz)) {
						throw new GGAPIEngineException("The class [" + clazz.getName()
								+ "] must implements the IGGAPIEntityWithTenant interface as it is mentionned as 'showTenantId'.");
					}
				}

				if (!IGGAPIDTOObject.class.isAssignableFrom(dtoClass)) {
					throw new GGAPIEngineException(
							"The class [" + dtoClass.getName() + "] must implements the IGGAPIDTOObject interface.");
				}

				GGAPIDao db = entityAnnotation.db();

				// Web Service
				String ws__ = entityAnnotation.ws();
				IGGAPIRestService<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> ws = null;

				if (ws__ != null && !ws__.isEmpty()) {
					ws = (IGGAPIRestService<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>) this
							.getObjectFromConfiguration(ws__, IGGAPIRestService.class);
				}

				// Controller
				String controller__ = entityAnnotation.controller();
				IGGAPIController<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> controller = null;

				if (controller__ != null && !controller__.isEmpty()) {
					controller = (IGGAPIController<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>) this
							.getObjectFromConfiguration(controller__, IGGAPIController.class);
				}

				// Event Publisher
				String event__ = entityAnnotation.eventPublisher();
				IGGAPIEventPublisher event = null;

				if (event__ != null && !event__.isEmpty()) {
					event = (IGGAPIEventPublisher) this.getObjectFromConfiguration(event__, IGGAPIEventPublisher.class);
				}

				// Business
				String business__ = entityAnnotation.business();
				IGGAPIBusiness<IGGAPIEntity> business = null;

				if (business__ != null && !business__.isEmpty()) {
					business = (IGGAPIBusiness<IGGAPIEntity>) this.getObjectFromConfiguration(business__,
							IGGAPIBusiness.class);
				}

				// Connector
				String connector__ = entityAnnotation.connector();
				IGGAPIConnector<IGGAPIEntity, List<IGGAPIEntity>, IGGAPIDTOObject<IGGAPIEntity>> connector = null;

				if (connector__ != null && !connector__.isEmpty()) {
					connector = (IGGAPIConnector<IGGAPIEntity, List<IGGAPIEntity>, IGGAPIDTOObject<IGGAPIEntity>>) this
							.getObjectFromConfiguration(connector__, IGGAPIConnector.class);
				}

				// Repository
				String repo__ = entityAnnotation.repository();
				IGGAPIRepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> repo = null;

				if (repo__ != null && !repo__.isEmpty()) {
					repo = (IGGAPIRepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>) this
							.getObjectFromConfiguration(repo__, IGGAPIRepository.class);
				}

				// DAO
				String dao__ = entityAnnotation.repository();
				IGGAPIDAORepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> dao = null;

				if (dao__ != null && !dao__.isEmpty()) {
					dao = (IGGAPIDAORepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>) this
							.getObjectFromConfiguration(dao__, IGGAPIDAORepository.class);
				}

				try {
					this.services.add(this.createDynamicDomain(domain, entityClass, dtoClass, db, ws, controller,
							business, event, connector, repo, dao, allow_creation, allow_read_all, allow_read_one,
							allow_update_one, allow_delete_one, allow_delete_all, allow_count, creation_access,
							read_all_access, read_one_access, update_one_access, delete_one_access, delete_all_access,
							count_access, creation_authority, read_all_authority, read_one_authority,
							update_one_authority, delete_one_authority, delete_all_authority, count_authority,
							hiddenable, publicEntity, shared, tenant, unicity, showTenantId));
				} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException | IOException e) {
					throw new GGAPIEngineException(e);
				}
			}
		}

		return services;
	}

	@SuppressWarnings("unchecked")
	private <T> T getObjectFromConfiguration(String objectName, Class<T> superClass) throws GGAPIEngineException {
		T obj = null;

		String[] splits = objectName.split(":");
		Class<?> objClass;
		try {
			objClass = Class.forName(splits[1]);
		} catch (ClassNotFoundException e1) {
			throw new GGAPIEngineException(e1);
		}

		if (!superClass.isAssignableFrom(objClass)) {
			throw new GGAPIEngineException("The class [" + objClass.getName() + "] must implements the ["
					+ superClass.getCanonicalName() + "] interface.");
		}

		switch (splits[0]) {
		case "bean":
			obj = (T) this.context.getBean(objClass);
			break;
		case "class":
			try {
				Constructor<T> ctor = (Constructor<T>) objClass.getConstructor();
				obj = ctor.newInstance();
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new GGAPIEngineException(e);
			}
			break;
		default:
			throw new GGAPIEngineException("Invalid controller " + objectName + ", should be bean: or class:");
		}

		return obj;
	}

	/**
	 * 
	 * @param services
	 * @param entityClass
	 * @param dtoClass
	 * @param db
	 * @param controller2
	 * @param ws
	 * @param event
	 * @param dynamicController
	 * @param connector
	 * @param repo
	 * @param dao
	 * @param allow_creation
	 * @param allow_read_all
	 * @param allow_read_one
	 * @param allow_update_one
	 * @param allow_delete_one
	 * @param allow_delete_all
	 * @param allow_count
	 * @param domain
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 */
	private IGGAPIRestService<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> createDynamicDomain(String domain,
			Class<IGGAPIEntity> entityClass, Class<IGGAPIDTOObject<IGGAPIEntity>> dtoClass, GGAPIDao db,
			IGGAPIRestService<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> ws,
			IGGAPIController<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> controller,
			IGGAPIBusiness<IGGAPIEntity> business, IGGAPIEventPublisher event,
			IGGAPIConnector<IGGAPIEntity, List<IGGAPIEntity>, IGGAPIDTOObject<IGGAPIEntity>> connector,
			IGGAPIRepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> repo,
			IGGAPIDAORepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> dao, boolean allow_creation,
			boolean allow_read_all, boolean allow_read_one, boolean allow_update_one, boolean allow_delete_one,
			boolean allow_delete_all, boolean allow_count, GGAPICrudAccess creation_access,
			GGAPICrudAccess read_all_access, GGAPICrudAccess read_one_access, GGAPICrudAccess update_one_access,
			GGAPICrudAccess delete_one_access, GGAPICrudAccess delete_all_access, GGAPICrudAccess count_access,
			boolean creation_authority, boolean read_all_authority, boolean read_one_authority,
			boolean update_one_authority, boolean delete_one_authority, boolean delete_all_authority,
			boolean count_authority, boolean hiddenable, boolean publicEntity, String shared, boolean tenantEntity,
			String[] unicity, boolean showTenantId) throws NoSuchMethodException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IOException {

		/*
		 * TODO: THIS METHOD NEEDS TO BE REFACTORED
		 */
		IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> domainObj = new IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>() {

			@Override
			public Class<IGGAPIEntity> getEntityClass() {
				return entityClass;
			}

			@Override
			public Class<IGGAPIDTOObject<IGGAPIEntity>> getDtoClass() {
				return dtoClass;
			}

			@Override
			public String getDomain() {
				return domain;
			}

		};

		log.info(
				"Creating Dynamic Domain {} [Entity [{}], DTO [{}], DB [{}], Public [{}], Shared [{}], Hiddenable [{}], allow_creation [{}], allow_read_all [{}], allow_read_one [{}], allow_update_one [{}], allow_delete_one [{}], allow_delete_all [{}], allow_count [{}]], creation_access [{}], read_all_access [{}], read_one_access [{}], update_one_access [{}], delete_one_access [{}], delete_all_access [{}], count_access [{}], creation_authority [{}], read_all_authority [{}], read_one_authority [{}], update_one_authority [{}], delete_one_authority [{}], delete_all_authority [{}], count_authority [{}]",
				domainObj.getDomain(), entityClass.getCanonicalName(), dtoClass.getCanonicalName(), db, publicEntity, shared, hiddenable, allow_creation,
				allow_read_all, allow_read_one, allow_update_one, allow_delete_one, allow_delete_all, allow_count,
				creation_access, read_all_access, read_one_access, update_one_access, delete_one_access,
				delete_all_access, count_access, creation_authority, read_all_authority, read_one_authority,
				update_one_authority, delete_one_authority, delete_all_authority, count_authority);

		if (connector != null) {
			connector.setDomain(domainObj);
		}

		Optional<IGGAPIConnector<IGGAPIEntity, List<IGGAPIEntity>, IGGAPIDTOObject<IGGAPIEntity>>> connectorObj = Optional
				.ofNullable(connector);
		Optional<IGGAPIBusiness<IGGAPIEntity>> businessObj = Optional.ofNullable(business);
		Optional<IGGAPIEventPublisher> eventObj = Optional.ofNullable(event);

		if (dao == null) {
			switch (db) {
			default:
			case mongo:
				if( this.mongo.isEmpty() ) {
					throw new InstantiationException("No mongo connection available.");
				}
				dao = new GGAPIEngineMongoRepository(domainObj, this.mongo.get(), this.magicTenantId);
				break;
			}
		} else {
			dao.setMagicTenantId(this.magicTenantId);
		}
		dao.setHiddenable(hiddenable);
		dao.setPublic(publicEntity);
		dao.setShared(shared);

		if (repo == null) {
			repo = new GGAPIEngineRepository(domainObj, dao);
		} else {
			repo.setDomain(domainObj);
			repo.setDao(dao);
		}

		Optional<IGGAPIRepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>> repoObj = Optional.ofNullable(repo);

		if (controller == null) {
			controller = new GGAPIEngineController(domainObj, repoObj, connectorObj, businessObj, tenantEntity);
		} else {
			controller.setDomain(domainObj);
			controller.setRepository(repoObj);
			controller.setConnector(connectorObj);
			controller.setBusiness(businessObj);
			controller.setTenant(tenantEntity);
		}
		controller.setUnicity(unicity);

		if (ws == null) {
			ws = new GGAPIEngineRestService(domainObj, controller);
		} else {
			ws.setDomain(domainObj);
			ws.setController(controller);
		}
		ws.setEventPublisher(eventObj);
		ws.allow(allow_creation, allow_read_all, allow_read_one, allow_update_one, allow_delete_one,
				allow_delete_all, allow_count);
		ws.setAccesses(creation_access, read_all_access, read_one_access, update_one_access, delete_one_access,
				delete_all_access, count_access);
		ws.setAuthorities(creation_authority, read_all_authority, read_one_authority, update_one_authority, delete_one_authority, delete_all_authority, count_authority);

		this.daos.put(domain.toLowerCase() + "_dao", dao);
		this.repositries.put(domain.toLowerCase() + "_repository", repo);
		this.controllers.put(domain.toLowerCase() + "_controller", controller);
		this.restServices.put(domain.toLowerCase() + "_service", ws);

		String baseUrl = "/" + domain.toLowerCase();

		RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
		options.setPatternParser(new PathPatternParser());

		RequestMappingInfo requestMappingInfoGetAll = RequestMappingInfo.paths(baseUrl).methods(RequestMethod.GET)
				.options(options).build();
		RequestMappingInfo requestMappingInfoDeleteAll = RequestMappingInfo.paths(baseUrl).methods(RequestMethod.DELETE)
				.options(options).build();
		RequestMappingInfo requestMappingInfoCreate = RequestMappingInfo.paths(baseUrl).methods(RequestMethod.POST)
				.options(options).build();
		RequestMappingInfo requestMappingInfoCount = RequestMappingInfo.paths(baseUrl + "/count")
				.methods(RequestMethod.GET).options(options).build();
		RequestMappingInfo requestMappingInfoGetOne = RequestMappingInfo.paths(baseUrl + "/{uuid}")
				.methods(RequestMethod.GET).options(options).build();
		RequestMappingInfo requestMappingInfoUpdate = RequestMappingInfo.paths(baseUrl + "/{uuid}")
				.methods(RequestMethod.PATCH).options(options).build();
		RequestMappingInfo requestMappingInfoDeleteOne = RequestMappingInfo.paths(baseUrl + "/{uuid}")
				.methods(RequestMethod.DELETE).options(options).build();

		if( this.openApi.isPresent() ) {
			
			Tag tag = new Tag().name("Domain " + domain.toLowerCase()).description("Public Entity ["+publicEntity+"] Shared Entity ["+(shared.isEmpty()?"false":shared)+"] Hiddenable Entity ["+hiddenable+"]");
			this.openApi.get().addTagsItem(tag);
	
			GGAPIEntity entityAnnotation = ((Class<IGGAPIEntity>) entityClass).getAnnotation(GGAPIEntity.class);
	
			OpenAPI templateOpenApi = this.openApiHelper.getOpenApi(domain.toLowerCase(), entityClass.getSimpleName(),
					entityAnnotation.openApiSchemas());
			PathItem pathItemBase = new PathItem();
			PathItem pathItemCount = new PathItem();
			PathItem pathItemUuid = new PathItem();
	
			this.openApi.get().getComponents().addSchemas(entityClass.getSimpleName(),
					templateOpenApi.getComponents().getSchemas().get(entityClass.getSimpleName()));
			this.openApi.get().getComponents().addSchemas("ErrorObject",
					templateOpenApi.getComponents().getSchemas().get("ErrorObject"));
			this.openApi.get().getComponents().addSchemas("SortQuery",
					templateOpenApi.getComponents().getSchemas().get("SortQuery"));
			this.openApi.get().getComponents().addSchemas("FilterQuery",
					templateOpenApi.getComponents().getSchemas().get("FilterQuery"));
	
			if (allow_read_all) {
				this.requestMappingHandlerMapping.registerMapping(requestMappingInfoGetAll, ws,
						ws.getClass().getMethod("getEntities", String.class, GGAPIReadOutputMode.class, Integer.class,
								Integer.class, String.class, String.class, String.class));
				this.openApi.get().path(baseUrl, pathItemBase.get(templateOpenApi.getPaths().get(baseUrl).getGet().description("Access : ["+read_all_access+"] - Authority ["+(creation_authority==false?"none":BasicGGAPIAuthorization.getAuthorization(domain.toLowerCase(), GGAPICrudOperation.read_all))+"]")));
			}
			if (allow_delete_all) {
				this.requestMappingHandlerMapping.registerMapping(requestMappingInfoDeleteAll, ws,
						ws.getClass().getMethod("deleteAll", String.class, String.class));
				this.openApi.get().path(baseUrl, pathItemBase.delete(templateOpenApi.getPaths().get(baseUrl).getDelete().description("Access : ["+delete_all_access+"] - Authority ["+(creation_authority==false?"none":BasicGGAPIAuthorization.getAuthorization(domain.toLowerCase(), GGAPICrudOperation.delete_all))+"]")));
			}
			if (allow_creation) {
				this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCreate, ws,
						ws.getClass().getMethod("createEntity", String.class, String.class, String.class));
				this.openApi.get().path(baseUrl, pathItemBase.post(templateOpenApi.getPaths().get(baseUrl).getPost().description("Access : ["+creation_access+"] - Authority ["+(creation_authority==false?"none":BasicGGAPIAuthorization.getAuthorization(domain.toLowerCase(), GGAPICrudOperation.create_one))+"]")));
			}
			if (allow_count) {
				this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCount, ws,
						ws.getClass().getMethod("getCount", String.class, String.class));
				this.openApi.get().path(baseUrl + "/count",
						pathItemCount.get(templateOpenApi.getPaths().get(baseUrl + "/count").getGet().description("Access : ["+count_access+"] - Authority ["+(creation_authority==false?"none":BasicGGAPIAuthorization.getAuthorization(domain.toLowerCase(), GGAPICrudOperation.count))+"]")));
			}
			if (allow_read_one) {
				this.requestMappingHandlerMapping.registerMapping(requestMappingInfoGetOne, ws,
						ws.getClass().getMethod("getEntity", String.class, String.class, String.class));
				this.openApi.get().path(baseUrl + "/{uuid}",
						pathItemUuid.get(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getGet().description("Access : ["+read_one_access+"] - Authority ["+(creation_authority==false?"none":BasicGGAPIAuthorization.getAuthorization(domain.toLowerCase(), GGAPICrudOperation.read_one))+"]")));
			}
			if (allow_update_one) {
				this.requestMappingHandlerMapping.registerMapping(requestMappingInfoUpdate, ws,
						ws.getClass().getMethod("updateEntity", String.class, String.class, String.class, String.class));
				this.openApi.get().path(baseUrl + "/{uuid}",
						pathItemUuid.patch(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getPatch().description("Access : ["+update_one_access+"] - Authority ["+(creation_authority==false?"none":BasicGGAPIAuthorization.getAuthorization(domain.toLowerCase(), GGAPICrudOperation.update_one))+"]")));
			}
			if (allow_delete_one) {
				this.requestMappingHandlerMapping.registerMapping(requestMappingInfoDeleteOne, ws,
						ws.getClass().getMethod("deleteEntity", String.class, String.class, String.class));
				this.openApi.get().path(baseUrl + "/{uuid}",
						pathItemUuid.delete(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getDelete().description("Access : ["+delete_one_access+"] - Authority ["+(creation_authority==false?"none":BasicGGAPIAuthorization.getAuthorization(domain.toLowerCase(), GGAPICrudOperation.delete_one))+"]")));
			}
	
			Info infos = this.openApi.get().getInfo();
			String description = infos.getDescription() + "       The configured Magic Tenant ID is : 0";
			infos.description(description);
	}

		return ws;
	}

}
