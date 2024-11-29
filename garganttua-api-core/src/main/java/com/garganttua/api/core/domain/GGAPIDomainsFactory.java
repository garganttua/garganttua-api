package com.garganttua.api.core.domain;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.domain.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIDomainsFactory {

	private List<String> packages;
	private boolean tenantFound = false;
	private Set<IGGAPIDomain> domains;

	public GGAPIDomainsFactory(List<String> packages) throws GGAPIEngineException {
		this.packages = packages;
		this.collectDomains();
	}

	public Collection<IGGAPIDomain> getDomains() {
		return this.domains;
	}

	private void collectDomains() throws GGAPIEngineException {
		log.info("*** Collecting Domains ...");
		if (this.packages == null) {
			throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "No packages");
		}

		this.domains = new HashSet<IGGAPIDomain>();

		this.packages.stream().forEach(package_ -> {
			try {
				List<Class<?>> annotatedClasses = GGObjectReflectionHelper.getClassesWithAnnotation(package_,
						GGAPIEntity.class);
				if (log.isDebugEnabled())
					log.debug("Found " + annotatedClasses.size() + " domains");

				annotatedClasses.forEach(annotatedClass -> {
					if (log.isDebugEnabled())
						log.debug("processing annotated entity " + annotatedClass.getSimpleName());
					try {
						this.domains.add(processAnnotatedEntity(annotatedClass));
					} catch (GGAPIException e) {
						e.printStackTrace();
					}
				});

			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		});

		if (!this.tenantFound) {
			throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "No tenant entity found !");
		}
	}

	private GGAPIDomain processAnnotatedEntity(Class<?> annotatedClass) throws GGAPIException {
		GGAPIDomain domain = GGAPIDomain.fromEntityClass(annotatedClass, this.packages);

		if (domain == null) {
			log.warn("Found entity " + annotatedClass.getName()
					+ " with annotation @GGAPIEntity, but unable to retrieve the corespounding dynamic domain. Ignoring");
			return null;
		}

		if (domain.getEntity().getValue1().tenantEntity() && !this.tenantFound) {
			this.tenantFound = true;
		} else if (domain.getEntity().getValue1().tenantEntity() && !this.tenantFound) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"There are more than one entity declared as tenantEntity.");
		}

		if (domain.getDtos().size() == 0) {
			log.error("No class annotated with @GGAPIDto found for entity " + annotatedClass.getName());
			throw new GGAPIDtoException(GGAPIExceptionCode.NO_DTO_FOUND,
					"No class annotated with @GGAPIDto found for entity " + annotatedClass.getName());
		}

		log.info("	Dynamic Domain Added " + domain.toString());
		return domain;
	}

	public IGGAPIDomainsRegistry getRegistry() {
		return new GGAPIDomainsRegistry(this.domains);
	}

}
