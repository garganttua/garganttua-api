package com.garganttua.api.spec.engine;

public interface IApplicationContext {

    void start();

    void stop();

    void flush();

    void init();

    void reload();

}
