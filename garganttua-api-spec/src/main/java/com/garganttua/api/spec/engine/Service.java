package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.Pluralizer;
import com.garganttua.api.spec.Singularizer;

public record Service(String domainName, String entityName, ServiceType serviceType,
        TechnicalOperation technicalOperation, Business business, Method method, Action action, Security security) {

    public String getServiceName() {
        if (action == Action.allEntities || action == Action.listOfEntities) {
            return technicalOperation + "-all-" + Pluralizer.toPlural(entityName);
        }
        if (action == Action.oneEntity) {
            return technicalOperation + "-one-" + Singularizer.toSingular(entityName);
        }
        return technicalOperation + "-one-" + Singularizer.toSingular(entityName);
    }

    public BusinessOperation getBusinessOperation() {
        switch (technicalOperation) {
            case create:
                return BusinessOperation.create;
            case delete:
                if (action == Action.oneEntity)
                    return BusinessOperation.deleteOne;
                else
                    return BusinessOperation.deleteAll;
            case read:
            default:
                if (action == Action.oneEntity)
                    return BusinessOperation.readOne;
                else
                    return BusinessOperation.readAll;

            case update:
                return BusinessOperation.update;
        }
    }
}
