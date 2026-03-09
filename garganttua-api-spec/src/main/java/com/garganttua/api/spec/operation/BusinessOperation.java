package com.garganttua.api.spec.operation;

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
    useCase("useCase"),
    workflow("workflow");

    @Getter
    private final String label;

    BusinessOperation(String label) {
        this.label = label;
    }
}
