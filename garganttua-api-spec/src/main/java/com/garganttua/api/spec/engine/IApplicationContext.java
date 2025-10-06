package com.garganttua.api.spec.engine;

import java.util.Optional;

public interface IApplicationContext {

    void start();

    void stop();

    void flush();

    void init();

    void reload();

    Optional<IDomainContext> getDomainContext(String domainName);

}
