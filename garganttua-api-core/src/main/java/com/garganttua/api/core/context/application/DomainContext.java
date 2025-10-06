package com.garganttua.api.core.context.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.core.context.Repository;
import com.garganttua.api.core.definition.DomainDefinition;
import com.garganttua.api.spec.engine.Action;
import com.garganttua.api.spec.engine.Business;
import com.garganttua.api.spec.engine.IDomainContext;
import com.garganttua.api.spec.engine.IDomainDtoContext;
import com.garganttua.api.spec.engine.IDomainEntityContext;
import com.garganttua.api.spec.engine.IMethodBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.Method;
import com.garganttua.api.spec.engine.Security;
import com.garganttua.api.spec.engine.Service;
import com.garganttua.api.spec.engine.ServiceType;
import com.garganttua.api.spec.engine.TechnicalOperation;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.security.IDomainSecurityContext;

import lombok.Getter;

public class DomainContext implements IDomainContext {

    private @Nonnull DomainDefinition domainDefinition;
    private @Nonnull List<IObjectSupplierBuilder<?>> interfaces;
    private @Nonnull List<IObjectSupplierBuilder<?>> events;
    private IDomainSecurityContext domainSecurityContext;
    /* private IApplicationSecurityContext applicationSecurityContext; */

    private List<Service> services = new ArrayList<>();
    private IDomainEntityContext domainEntityContext;
    private @Nonnull List<IDomainDtoContext> dtoContexts;
    @Getter
    private IRepository repository = null;

    public DomainContext(DomainDefinition domainDefinition, IDomainEntityContext domainEntityContext,
            IDomainSecurityContext domainSecurityContext/* , IApplicationSecurityContext applicationSecurityContext */,
            List<IDomainDtoContext> dtoContexts,
            List<IObjectSupplierBuilder<?>> interfaces,
            List<IObjectSupplierBuilder<?>> events) {
        /*
         * this.applicationSecurityContext =
         * Objects.requireNonNull(applicationSecurityContext,
         * "Application security context cannot be null");private
         */
        this.domainSecurityContext = Objects.requireNonNull(domainSecurityContext,
                "Domain security context cannot be null");
        this.domainEntityContext = Objects.requireNonNull(domainEntityContext, "Domain entity context cannot be null");
        this.domainDefinition = Objects.requireNonNull(domainDefinition, "Domain definition cannot be null");
        this.interfaces = Objects.requireNonNull(interfaces, "Interfaces cannot be null");
        this.events = Objects.requireNonNull(events, "Events cannot be null");
        this.dtoContexts = Objects.requireNonNull(dtoContexts, "Dto contexts cannot be null");

        this.repository = new Repository(this.dtoContexts, domainEntityContext.getEntityClass());

        this.buildServices();

    }

    private void buildServices() {
        Business business = new Business(this.domainDefinition.publik(), this.domainDefinition.owner(), this.domainDefinition.owned(), this.domainDefinition.shared(), this.domainDefinition.hiddenable());

        if (this.domainDefinition.activateAllowReadAll()) {
            this.services.add(new Service(this.getDomainName(), this.domainEntityContext.getEntityName(), ServiceType.standard, TechnicalOperation.read, business, new Method(), Action.listOfEntities, new Security(false, null)));
        }
        if (this.domainDefinition.activateCreation()) {
            this.services.add(new Service(this.getDomainName(), this.domainEntityContext.getEntityName(), ServiceType.standard, TechnicalOperation.create, business, new Method(), Action.oneEntity, new Security(false, null)));
        }
        if (this.domainDefinition.activateDeleteAll()) {
            this.services.add(new Service(this.getDomainName(), this.domainEntityContext.getEntityName(), ServiceType.standard, TechnicalOperation.delete, business, new Method(), Action.allEntities, new Security(false, null)));
        }
        if (this.domainDefinition.activateDeleteOne()) {
            this.services.add(new Service(this.getDomainName(), this.domainEntityContext.getEntityName(), ServiceType.standard, TechnicalOperation.delete, business, new Method(), Action.oneEntity, new Security(false, null)));
        }
        if (this.domainDefinition.activateReadOne()) {
            this.services.add(new Service(this.getDomainName(), this.domainEntityContext.getEntityName(), ServiceType.standard, TechnicalOperation.read, business, new Method(), Action.oneEntity, new Security(false, null)));
        }
        if (this.domainDefinition.activateUpdate()) {
            this.services.add(new Service(this.getDomainName(), this.domainEntityContext.getEntityName(), ServiceType.standard, TechnicalOperation.update, business, new Method(), Action.oneEntity, new Security(false, null)));
        }

    }

    @Override
    public String getDomainName() {
        return this.domainDefinition.domainName();
    }

    @Override
    public List<Service> getServices() {
        return this.services;
    }

    @Override
    public List<IMethodBinderBuilder<?, ?>> getAfterGetMethods() {
        return this.domainEntityContext.getAfterGetMethods();
    }

}
