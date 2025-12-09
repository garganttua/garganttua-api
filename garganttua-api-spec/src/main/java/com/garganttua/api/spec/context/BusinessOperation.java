package com.garganttua.api.spec.context;

import lombok.Getter;

@Getter
public enum BusinessOperation {

    create("create"),
    update("update"),
    readOne("readOne"),
    readAll("readAll"),
    deleteOne("deleteOne"),
    deleteAll("deleteAll"),
    authenticate("authenticate"),
    useCase("useCase");

    @Getter
    private final String label;

    BusinessOperation(String label) {
        this.label = label;
    }
}
