package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.dao.IDao;

public interface IDtoBuilder extends IAutomaticLinkedBuilder<Object, IDomainBuilder, IDtoBuilder> {

    IDtoBuilder db(IObjectSupplierBuilder<?> daoSupplier);

    IDtoBuilder db(IDao dao);

}
