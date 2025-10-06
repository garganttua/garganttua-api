package com.garganttua.api.spec.engine;

import java.util.Optional;

public enum StandardParameter {

    UUID("uuid"),
    FILTER("filter"),
    SORT("sort"),
    PAGE("page"),
    MODE("mode");

    private final String label;

    StandardParameter(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static boolean isStandard(String paramName) {
        if (paramName == null) {
            return false;
        }
        for (StandardParameter p : values()) {
            if (p.label.equals(paramName)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<StandardParameter> fromLabel(String label) {
        if (label == null) {
            return Optional.empty();
        }
        for (StandardParameter p : values()) {
            if (p.label.equals(label)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }
}
