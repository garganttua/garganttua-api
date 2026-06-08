package com.garganttua.api.commons.endpoint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.core.reflection.annotations.Indexed;
import com.garganttua.core.reflection.annotations.Reflected;

/**
 * Marks a class as an {@link IInterface} (a transport entry point) so the
 * framework CATALOGS it — discoverable + reflectable (native-ready) — without
 * attaching it anywhere. Attachment stays explicit: declare it on the domains it
 * serves with {@code domain().interfasse(MyInterface.class)} (or a configured
 * {@code .interfasse(supplier)} for a pre-built instance). Mirrors {@code @Protocol}
 * / {@code @Serializer}, but discovery only registers the type — it never
 * auto-binds it to a domain.
 */
@Indexed
@Reflected
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Interface {
}
