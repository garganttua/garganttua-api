package com.garganttua.api.core.legacy.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.garganttua.api.core.legacy.dto.exceptions.DtoException;
import com.garganttua.api.core.legacy.engine.EngineException;
import com.garganttua.api.core.legacy.entity.exceptions.EntityException;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.domain.IDomainsRegistry;
import com.garganttua.api.spec.entity.annotations.Entity;
import com.garganttua.core.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DomainsFactory {

	private List<String> packages;
	private boolean tenantFound = false;
	private Set<IDomain> domains;

	public DomainsFactory(List<String> packages) throws EngineException {
		this.packages = packages;
		this.collectDomains();
	}

	public Collection<IDomain> getDomains() {
		return this.domains;
	}

	private void collectDomains() throws EngineException {
		log.info("*** Collecting Domains ...");
		if (this.packages == null) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "No packages");
		}

		this.domains = new HashSet<IDomain>();

		this.packages.stream().forEach(package_ -> {
			List<Class<?>> annotatedClasses = GGObjectReflectionHelper.getClassesWithAnnotation(package_,
					Entity.class);
			if (log.isDebugEnabled())
				log.debug("Found " + annotatedClasses.size() + " domains");

			annotatedClasses.forEach(annotatedClass -> {
				if (log.isDebugEnabled())
					log.debug("processing annotated entity " + annotatedClass.getSimpleName());
				try {
					this.domains.add(processAnnotatedEntity(annotatedClass));
				} catch (CoreException e) {
					e.printStackTrace();
				}
			});

		});

		if (!this.tenantFound) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "No tenant entity found !");
		}
	}

	private Domain processAnnotatedEntity(Class<?> annotatedClass) throws CoreException {
		Domain domain = Domain.fromEntityClass(annotatedClass, this.packages);

		if (domain == null) {
			log.warn("Found entity " + annotatedClass.getName()
					+ " with annotation @Entity, but unable to retrieve the corespounding dynamic domain. Ignoring");
			return null;
		}

		if (domain.getEntity().getValue1().tenantEntity() && !this.tenantFound) {
			this.tenantFound = true;
		} else if (domain.getEntity().getValue1().tenantEntity() && !this.tenantFound) {
			throw new EntityException(CoreExceptionCode.ENTITY_DEFINITION,
					"There are more than one entity declared as tenantEntity.");
		}

		if (domain.getDtos().size() == 0) {
			log.error("No class annotated with @Dto found for entity " + annotatedClass.getName());
			throw new DtoException(CoreExceptionCode.NO_DTO_FOUND,
					"No class annotated with @Dto found for entity " + annotatedClass.getName());
		}

		log.info("	Dynamic Domain Added " + domain.toString());
		return domain;
	}

	public IDomainsRegistry getRegistry() {
		return new DomainsRegistry(this.domains);
	}

}
