package com.garganttua.api.commons.context.dsl;

import java.lang.annotation.Annotation;
import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IMethod;

import com.garganttua.api.commons.context.IEntityContext;
import com.garganttua.api.commons.entity.IUuidGenerator;
import com.garganttua.api.commons.entity.annotations.UnicityScope;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;

public interface IEntityBuilder<E> extends IAutomaticLinkedBuilder<IEntityBuilder<E>, IDomainBuilder<E>, IEntityContext<E>> {

    IEntityBuilder<E> id(String string) throws ApiException;

    IEntityBuilder<E> id(IField field) throws ApiException;

    IEntityBuilder<E> id(ObjectAddress fieldAddress) throws ApiException;

    IEntityBuilder<E> uuid(String string) throws ApiException;

    IEntityBuilder<E> uuid(IField field) throws ApiException;

    IEntityBuilder<E> uuid(ObjectAddress fieldAddress) throws ApiException;

    /**
     * When {@code true}, the framework (re)generates the uuid at creation even if the
     * client supplied one — the client value is discarded. Default {@code false}
     * (a client-supplied uuid is kept; a missing one is generated).
     */
    IEntityBuilder<E> overwriteUuid(boolean overwrite);

    /**
     * Declares a custom uuid generator for this domain, used wherever the framework
     * assigns the uuid (no client value, or {@link #overwriteUuid(boolean)} on). Default:
     * a time-ordered UUID v7.
     */
    IEntityBuilder<E> uuidGenerator(IUuidGenerator generator);

    IEntityBuilder<E> tenantId(String string) throws ApiException;

    IEntityBuilder<E> tenantId(IField field) throws ApiException;

    IEntityBuilder<E> tenantId(ObjectAddress fieldAddress) throws ApiException;

    IEntityBuilder<E> mandatory(IField field) throws ApiException;

    IEntityBuilder<E> mandatory(String string) throws ApiException;

    IEntityBuilder<E> mandatory(ObjectAddress fieldAddress) throws ApiException;

    IEntityBuilder<E> unicity(IField field) throws ApiException;

    IEntityBuilder<E> unicity(String string) throws ApiException;

    IEntityBuilder<E> unicity(ObjectAddress fieldAddress) throws ApiException;

    IEntityBuilder<E> unicity(String string, UnicityScope system) throws ApiException;

    IEntityBuilder<E> unicity(IField field, UnicityScope system) throws ApiException;

    IEntityBuilder<E> unicity(ObjectAddress fieldAddress, UnicityScope system) throws ApiException;

    /**
     * Declares a field a caller may valorize at CREATION (no authority required). Declaring any
     * {@code create(...)} turns creation into a WHITELIST: only declared fields are kept from the
     * client body, every other client-supplied field is stripped before persist. With no
     * {@code create(...)} declared at all, creation is unrestricted (the client body is kept as-is).
     * The CREATE-time analogue of {@link #update(String)}.
     */
    IEntityBuilder<E> create(String string) throws ApiException;

    IEntityBuilder<E> create(IField field) throws ApiException;

    IEntityBuilder<E> create(ObjectAddress fieldAddress) throws ApiException;

    /**
     * Declares a field a caller may valorize at CREATION only when it carries {@code authority};
     * otherwise the field is stripped from the created entity. The CREATE-time analogue of
     * {@link #update(String, String)}.
     */
    IEntityBuilder<E> create(String string, String authority) throws ApiException;

    IEntityBuilder<E> create(IField field, String authority) throws ApiException;

    IEntityBuilder<E> create(ObjectAddress fieldAddress, String authority) throws ApiException;

    IEntityBuilder<E> update(String string) throws ApiException;

    IEntityBuilder<E> update(IField field) throws ApiException;

    IEntityBuilder<E> update(ObjectAddress fieldAddress) throws ApiException;

    IEntityBuilder<E> update(String string, String authority) throws ApiException;

    IEntityBuilder<E> update(IField field, String authority) throws ApiException;

    IEntityBuilder<E> update(ObjectAddress fieldAddress, String authority) throws ApiException;

    IEntityBuilder<E> annotation(String elementName, IClass<? extends Annotation> annotation) throws ApiException;

    IEntityBuilder<E> annotation(IField field, IClass<? extends Annotation> annotation) throws ApiException;

    IEntityBuilder<E> annotation(ObjectAddress elementAddress, IClass<? extends Annotation> annotation) throws ApiException;

    IEntityBuilder<E> annotation(IMethod method, IClass<? extends Annotation> annotation) throws ApiException;

    IEntityMethodBinderBuilder<E> afterGet(String methodName) throws ApiException;

    IEntityMethodBinderBuilder<E> afterGet(IMethod method) throws ApiException;

    IEntityMethodBinderBuilder<E> afterGet(ObjectAddress methodAddress) throws ApiException;

    IEntityMethodBinderBuilder<E> beforeCreate(String methodName) throws ApiException;

    IEntityMethodBinderBuilder<E> beforeCreate(IMethod method) throws ApiException;

    IEntityMethodBinderBuilder<E> beforeCreate(ObjectAddress methodAddress) throws ApiException;

    IEntityMethodBinderBuilder<E> beforeUpdate(String methodName) throws ApiException;

    IEntityMethodBinderBuilder<E> beforeUpdate(IMethod method) throws ApiException;

    IEntityMethodBinderBuilder<E> beforeUpdate(ObjectAddress fieldmethodAddressAddress) throws ApiException;

    IEntityMethodBinderBuilder<E> beforeDelete(String methodName) throws ApiException;

    IEntityMethodBinderBuilder<E> beforeDelete(IMethod method) throws ApiException;

    IEntityMethodBinderBuilder<E> beforeDelete(ObjectAddress methodAddress) throws ApiException;

    IEntityMethodBinderBuilder<E> afterCreate(String methodName) throws ApiException;

    IEntityMethodBinderBuilder<E> afterCreate(IMethod method) throws ApiException;

    IEntityMethodBinderBuilder<E> afterCreate(ObjectAddress methodAddress) throws ApiException;

    IEntityMethodBinderBuilder<E> afterUpdate(String methodName) throws ApiException;

    IEntityMethodBinderBuilder<E> afterUpdate(IMethod method) throws ApiException;

    IEntityMethodBinderBuilder<E> afterUpdate(ObjectAddress methodAddress) throws ApiException;

    IEntityMethodBinderBuilder<E> afterDelete(String methodName) throws ApiException;

    IEntityMethodBinderBuilder<E> afterDelete(IMethod method) throws ApiException;

    IEntityMethodBinderBuilder<E> afterDelete(ObjectAddress methodAddress) throws ApiException;

}
