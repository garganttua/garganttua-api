package com.garganttua.api.core.context.application;

import java.lang.reflect.Method;
import java.security.Provider.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.swing.Action;

import com.garganttua.api.core.context.Repository;
import com.garganttua.api.core.definition.DomainDefinition;
import com.garganttua.api.spec.context.Business;
import com.garganttua.api.spec.context.IDomainDtoContext;
import com.garganttua.api.spec.context.IDomainEntityContext;
import com.garganttua.api.spec.context.IDtoContext;
import com.garganttua.api.spec.context.IEntityContext;
import com.garganttua.api.spec.context.ServiceType;
import com.garganttua.api.spec.context.TechnicalOperation;
import com.garganttua.api.spec.domain.IDomainContext;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.security.IDomainSecurityContext;
import com.garganttua.api.spec.security.Security;
import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;

import lombok.Getter;

public class DomainContext<E> implements IDomainContext<E> {

    private @Nonnull DomainDefinition<E> domainDefinition;
    private @Nonnull List<IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>>> interfaces;
    private @Nonnull List<IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>>> events;
    private IDomainSecurityContext domainSecurityContext;
    /* private IApplicationSecurityContext applicationSecurityContext; */

    private List<Service> services = new ArrayList<>();
    private IDomainEntityContext domainEntityContext;
    private @Nonnull List<IDomainDtoContext> dtoContexts;
    @Getter
    private IRepository repository = null;

    public DomainContext(DomainDefinition<E> domainDefinition, IEntityContext<E> domainEntityContext,
            IDomainSecurityContext domainSecurityContext/* , IApplicationSecurityContext applicationSecurityContext */,
            List<IDtoContext> dtoContexts,
            List<IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>>> interfaces,
            List<IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>>> events) {
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
    public List<IMethodBinderBuilder<?, ?, ?, ?>> getAfterGetMethods() {
        return this.domainEntityContext.getAfterGetMethods();
    }

}
