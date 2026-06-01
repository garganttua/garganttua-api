package com.garganttua.api.commons.operation;

public enum BusinessOperation {

    create("create"),
    update("update"),
    readOne("readOne"),
    readAll("readAll"),
    deleteOne("deleteOne"),
    deleteAll("deleteAll"),
    authenticate("authenticate"),
    refreshAuthorization("refreshAuthorization"),
    useCase("useCase"),
    workflow("workflow");

    private final String label;

    BusinessOperation(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}
