package com.garganttua.api.spec.engine;

public interface IContext {

    void start();

    void stop();

    void flush();

    void init();

    void reload();

}
