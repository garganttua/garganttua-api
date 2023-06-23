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

import javax.inject.Inject;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import com.garganttua.api.business.ISpringCrudifyBusiness;
import com.garganttua.api.connector.ISpringCrudifyConnector;
import com.garganttua.api.controller.ISpringCrudifyController;
import com.garganttua.api.controller.SpringCrudifyEngineController;
import com.garganttua.api.events.ISpringCrudifyEventPublisher;
import com.garganttua.api.repository.ISpringCrudifyRepository;
import com.garganttua.api.repository.SpringCrudifyEngineRepository;
import com.garganttua.api.repository.dao.ISpringCrudifyDAORepository;
import com.garganttua.api.repository.dao.SpringCrudifyDao;
import com.garganttua.api.repository.dao.mongodb.SpringCrudifyEngineMongoRepository;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.spec.ISpringCrudifyDomain;
import com.garganttua.api.spec.ISpringCrudifyEntity;
import com.garganttua.api.spec.SpringCrudifyEntity;
import com.garganttua.api.spec.SpringCrudifyEntityHelper;
import com.garganttua.api.spec.SpringCrudifyReadOutputMode;
import com.garganttua.api.ws.ISpringCrudifyRestService;
import com.garganttua.api.ws.SpringCrudifyEngineRestService;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SpringCrudifyDynamicDomainEngine implements ISpringCrudifyDynamicDomainEngine {

	@Inject
	protected Optional<MongoTemplate> mongo;

	@Inject
	protected ApplicationContext context;

	@Value("${spring.domain.crudify.magicTenantId}")
	protected String magicTenantId;

	@Value("${spring.domain.crudify.engine.packages}")
	protected String[] scanPackages;

	@Inject
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Inject
	public OpenAPI openApi;

	private List<ISpringCrudifyRestService<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>>> services;

	private Map<String, ISpringCrudifyDAORepository<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>>> daos = new HashMap<String, ISpringCrudifyDAORepository<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>>>();
	private Map<String, ISpringCrudifyRepository<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>>> repositries = new HashMap<String, ISpringCrudifyRepository<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>>>();
	private Map<String, ISpringCrudifyController<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>>> controllers = new HashMap<String, ISpringCrudifyController<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>>>();
	private Map<String, ISpringCrudifyRestService<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>>> restServices = new HashMap<String, ISpringCrudifyRestService<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>>>();

	private SpringCrudifyOpenAPIHelper openApiHelper;

	@Override
	public ISpringCrudifyDAORepository<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>> getDao(
			String name) {
		return this.daos.get(name);
	}

	@Override
	public ISpringCrudifyRepository<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>> getRepository(
			String name) {
		return this.repositries.get(name);
	}

	@Override
	public ISpringCrudifyController<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>> getController(
			String name) {
		return this.controllers.get(name);
	}

	@Override
	public ISpringCrudifyRestService<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>> getService(
			String name) {
		return this.restServices.get(name);
	}

	@SuppressWarnings({ "unchecked" })
	@Bean
	protected List<ISpringCrudifyRestService<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>>> engineServices()
			throws SpringCrudifyEngineException {

		this.openApiHelper = new SpringCrudifyOpenAPIHelper();

		log.info("============================================");
		log.info("======                                ======");
		log.info("====== Starting Dynamic Domain Engine ======");
		log.info("======                                ======");
		log.info("============================================");
		log.info("Version: {}", this.getClass().getPackage().getImplementationVersion());

		this.services = new ArrayList<ISpringCrudifyRestService<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>>>();

		for (String pack : this.scanPackages) {
			log.info("Scanning package " + pack);

			Reflections reflections = new Reflections(pack);

			Set<Class<?>> entities__ = reflections.getTypesAnnotatedWith(SpringCrudifyEntity.class);

			for (Class<?> clazz : entities__) {

				if (!ISpringCrudifyEntity.class.isAssignableFrom(clazz)) {
					throw new SpringCrudifyEngineException("The class [" + clazz.getName()
							+ "] must implements the ISpringCrudifyEntity interface.");
				}
				
				Class<ISpringCrudifyEntity> entityClass = (Class<ISpringCrudifyEntity>) clazz;
				Class<ISpringCrudifyDTOObject<ISpringCrudifyEntity>> dtoClass = null;

				SpringCrudifyEntity entityAnnotation = clazz.getAnnotation(SpringCrudifyEntity.class);

				boolean authorize_creation = entityAnnotation.authorize_creation();
				boolean authorize_read_all = entityAnnotation.authorize_read_all();
				boolean authorize_read_one = entityAnnotation.authorize_read_one();
				boolean authorize_update_one = entityAnnotation.authorize_update_one();
				boolean authorize_delete_one = entityAnnotation.authorize_delete_one();
				boolean authorize_delete_all = entityAnnotation.authorize_delete_all();
				boolean authorize_count = entityAnnotation.authorize_count();
				
				String domain = SpringCrudifyEntityHelper.getDomain(entityClass);
				
				try {
					dtoClass = (Class<ISpringCrudifyDTOObject<ISpringCrudifyEntity>>) Class.forName(entityAnnotation.dto());
				} catch (ClassNotFoundException e) {
					throw new SpringCrudifyEngineException(e);
				}

				if (!ISpringCrudifyDTOObject.class.isAssignableFrom(dtoClass)) {
					throw new SpringCrudifyEngineException("The class [" + dtoClass.getName()
							+ "] must implements the ISpringCrudifyDTOObject interface.");
				}

				SpringCrudifyDao db = entityAnnotation.db();

				// Web Service
				String ws__ = entityAnnotation.ws();
				ISpringCrudifyRestService<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> ws = null;

				if (ws__ != null && !ws__.isEmpty()) {
					ws = (ISpringCrudifyRestService<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>>) this.getObjectFromConfiguration(ws__, ISpringCrudifyRestService.class);
				}

				// Controller
				String controller__ = entityAnnotation.controller();
				ISpringCrudifyController<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> controller = null;

				if (controller__ != null && !controller__.isEmpty()) {
					controller = (ISpringCrudifyController<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>>) this
							.getObjectFromConfiguration(controller__, ISpringCrudifyController.class);
				}

				// Event Publisher
				String event__ = entityAnnotation.eventPublisher();
				ISpringCrudifyEventPublisher event = null;

				if (event__ != null && !event__.isEmpty()) {
					event = (ISpringCrudifyEventPublisher) this.getObjectFromConfiguration(event__,
							ISpringCrudifyEventPublisher.class);
				}

				// Business
				String business__ = entityAnnotation.business();
				ISpringCrudifyBusiness<ISpringCrudifyEntity> business = null;

				if (business__ != null && !business__.isEmpty()) {
					business = (ISpringCrudifyBusiness<ISpringCrudifyEntity>) this
							.getObjectFromConfiguration(business__, ISpringCrudifyBusiness.class);
				}

				// Connector
				String connector__ = entityAnnotation.connector();
				ISpringCrudifyConnector<ISpringCrudifyEntity, List<ISpringCrudifyEntity>, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> connector = null;

				if (connector__ != null && !connector__.isEmpty()) {
					connector = (ISpringCrudifyConnector<ISpringCrudifyEntity, List<ISpringCrudifyEntity>, ISpringCrudifyDTOObject<ISpringCrudifyEntity>>) this
							.getObjectFromConfiguration(connector__, ISpringCrudifyConnector.class);
				}

				// Repository
				String repo__ = entityAnnotation.repository();
				ISpringCrudifyRepository<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> repo = null;

				if (repo__ != null && !repo__.isEmpty()) {
					repo = (ISpringCrudifyRepository<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>>) this
							.getObjectFromConfiguration(repo__, ISpringCrudifyRepository.class);
				}

				// DAO
				String dao__ = entityAnnotation.repository();
				ISpringCrudifyDAORepository<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> dao = null;

				if (dao__ != null && !dao__.isEmpty()) {
					dao = (ISpringCrudifyDAORepository<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>>) this
							.getObjectFromConfiguration(dao__, ISpringCrudifyDAORepository.class);
				}

				try {
					this.services.add( this.createDynamicDomain(domain, entityClass, dtoClass, db, ws, controller, business, event,
							connector, repo, dao, authorize_creation, authorize_read_all, authorize_read_one,
							authorize_update_one, authorize_delete_one, authorize_delete_all, authorize_count));
				} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException | IOException e) {
					throw new SpringCrudifyEngineException(e);
				}
			}
		}

		return services;
	}

	@SuppressWarnings("unchecked")
	private <T> T getObjectFromConfiguration(String objectName, Class<T> superClass) throws SpringCrudifyEngineException {
		T obj = null;

		String[] splits = objectName.split(":");
		Class<?> objClass;
		try {
			objClass = Class.forName(splits[1]);
		} catch (ClassNotFoundException e1) {
			throw new SpringCrudifyEngineException(e1);
		}

		if (!superClass.isAssignableFrom(objClass)) {
			throw new SpringCrudifyEngineException("The class [" + objClass.getName() + "] must implements the ["
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
				throw new SpringCrudifyEngineException(e);
			}
			break;
		default:
			throw new SpringCrudifyEngineException("Invalid controller " + objectName + ", should be bean: or class:");
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
	 * @param authorize_creation
	 * @param authorize_read_all
	 * @param authorize_read_one
	 * @param authorize_update_one
	 * @param authorize_delete_one
	 * @param authorize_delete_all
	 * @param authorize_count
	 * @param domain 
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 */
	private ISpringCrudifyRestService<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> createDynamicDomain(
			String domain,
			Class<ISpringCrudifyEntity> entityClass, 
			Class<ISpringCrudifyDTOObject<ISpringCrudifyEntity>> dtoClass,
			SpringCrudifyDao db, 
			ISpringCrudifyRestService<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> ws,
			ISpringCrudifyController<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> controller,
			ISpringCrudifyBusiness<ISpringCrudifyEntity> business, 
			ISpringCrudifyEventPublisher event,
			ISpringCrudifyConnector<ISpringCrudifyEntity, List<ISpringCrudifyEntity>, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> connector, 
			ISpringCrudifyRepository<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> repo,
			ISpringCrudifyDAORepository<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> dao, 
			boolean authorize_creation,
			boolean authorize_read_all, 
			boolean authorize_read_one, 
			boolean authorize_update_one,
			boolean authorize_delete_one, 
			boolean authorize_delete_all, 
			boolean authorize_count)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException {

		/*
		 * TODO: THIS METHOD NEEDS TO BE REFACTORED
		 */
		ISpringCrudifyDomain<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> domainObj = new ISpringCrudifyDomain<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>>() {

			@Override
			public Class<ISpringCrudifyEntity> getEntityClass() {
				return entityClass;
			}

			@Override
			public Class<ISpringCrudifyDTOObject<ISpringCrudifyEntity>> getDtoClass() {
				return dtoClass;
			}

			@Override
			public String getDomain() {
				return domain;
			}
			
		};
		
		log.info(
				"Creating Dynamic Domain {} [Entity [{}], DTO [{}], DB [{}], authorize_creation [{}], authorize_read_all [{}], authorize_read_one [{}], authorize_update_one [{}], authorize_delete_one [{}], authorize_delete_all [{}], authorize_count [{}]]",
				domainObj.getDomain(), entityClass.getCanonicalName(), dtoClass.getCanonicalName(), db, authorize_creation,
				authorize_read_all, authorize_read_one, authorize_update_one, authorize_delete_one,
				authorize_delete_all, authorize_count);

		if( connector != null ) {
			connector.setDomain(domainObj);
		}
		
		Optional<ISpringCrudifyConnector<ISpringCrudifyEntity, List<ISpringCrudifyEntity>, ISpringCrudifyDTOObject<ISpringCrudifyEntity>>> connectorObj = Optional
				.ofNullable(connector);
		Optional<ISpringCrudifyBusiness<ISpringCrudifyEntity>> businessObj = Optional.ofNullable(business);
		Optional<ISpringCrudifyEventPublisher> eventObj = Optional.ofNullable(event);

		if (dao == null) {
			switch (db) {
			default:
			case mongo:
				dao = new SpringCrudifyEngineMongoRepository(domainObj, this.mongo.get(), this.magicTenantId);
				break;
			}
		} else {
			dao.setMagicTenantId(this.magicTenantId);
		}

		if (repo == null) {
			repo = new SpringCrudifyEngineRepository(domainObj, dao);
		} else {
			repo.setDomain(domainObj);
			repo.setDao(dao);
		}

		Optional<ISpringCrudifyRepository<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>>> repoObj = Optional.ofNullable(repo);

		if (controller == null) {
			controller = new SpringCrudifyEngineController(domainObj, repoObj, connectorObj, businessObj, eventObj);
		} else {
			controller.setDomain(domainObj);
			controller.setRepository(repoObj);
			controller.setConnector(connectorObj);
			controller.setBusiness(businessObj);
			controller.setEventPublisher(eventObj);
		}

		if (ws == null) {
			ws = new SpringCrudifyEngineRestService(domainObj, controller, authorize_creation, authorize_read_all,
					authorize_read_one, authorize_update_one, authorize_delete_one, authorize_delete_all,
					authorize_count);
		} else {
			ws.setDomain(domainObj);
			ws.setController(controller);
			ws.authorize(authorize_creation, authorize_read_all, authorize_read_one, authorize_update_one, authorize_delete_one, authorize_delete_all, authorize_count);
		}

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

		Tag tag = new Tag().name("Domain " + domain.toLowerCase());
		this.openApi.addTagsItem(tag);

		SpringCrudifyEntity entityAnnotation = ((Class<ISpringCrudifyEntity>) entityClass)
				.getAnnotation(SpringCrudifyEntity.class);

		OpenAPI templateOpenApi = this.openApiHelper.getOpenApi(domain.toLowerCase(), entityClass.getSimpleName(),
				entityAnnotation.openApiSchemas());
		PathItem pathItemBase = new PathItem();
		PathItem pathItemCount = new PathItem();
		PathItem pathItemUuid = new PathItem();

		this.openApi.getComponents().addSchemas(entityClass.getSimpleName(),
				templateOpenApi.getComponents().getSchemas().get(entityClass.getSimpleName()));
		this.openApi.getComponents().addSchemas("ErrorObject",
				templateOpenApi.getComponents().getSchemas().get("ErrorObject"));
		this.openApi.getComponents().addSchemas("SortQuery",
				templateOpenApi.getComponents().getSchemas().get("SortQuery"));
		this.openApi.getComponents().addSchemas("FilterQuery",
				templateOpenApi.getComponents().getSchemas().get("FilterQuery"));

		if (authorize_read_all) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoGetAll, ws,
					ws.getClass().getMethod("getEntities", String.class, SpringCrudifyReadOutputMode.class,
							Integer.class, Integer.class, String.class, String.class));
			this.openApi.path(baseUrl, pathItemBase.get(templateOpenApi.getPaths().get(baseUrl).getGet()));
		}
		if (authorize_delete_all) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoDeleteAll, ws,
					ws.getClass().getMethod("deleteAll", String.class));
			this.openApi.path(baseUrl, pathItemBase.delete(templateOpenApi.getPaths().get(baseUrl).getDelete()));
		}
		if (authorize_creation) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCreate, ws,
					ws.getClass().getMethod("createEntity", String.class, String.class));
			this.openApi.path(baseUrl, pathItemBase.post(templateOpenApi.getPaths().get(baseUrl).getPost()));
		}
		if (authorize_count) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCount, ws,
					ws.getClass().getMethod("getCount", String.class));
			this.openApi.path(baseUrl + "/count",
					pathItemCount.get(templateOpenApi.getPaths().get(baseUrl + "/count").getGet()));
		}
		if (authorize_read_one) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoGetOne, ws,
					ws.getClass().getMethod("getEntity", String.class, String.class));
			this.openApi.path(baseUrl + "/{uuid}",
					pathItemUuid.get(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getGet()));
		}
		if (authorize_update_one) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoUpdate, ws,
					ws.getClass().getMethod("updateEntity", String.class, String.class, String.class));
			this.openApi.path(baseUrl + "/{uuid}",
					pathItemUuid.patch(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getPatch()));
		}
		if (authorize_delete_one) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoDeleteOne, ws,
					ws.getClass().getMethod("deleteEntity", String.class, String.class));
			this.openApi.path(baseUrl + "/{uuid}",
					pathItemUuid.delete(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getDelete()));
		}

		Info infos = this.openApi.getInfo();
		String description = infos.getDescription() + "       The configured Magic Tenant ID is : 0";
		infos.description(description);

		return ws;
	}

}
