package com.garganttua.api.commons.operation;

public enum Scope {
    allEntities("all"), oneEntity("one"), listOfEntities("listOf");

    private String label;

    Scope(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
