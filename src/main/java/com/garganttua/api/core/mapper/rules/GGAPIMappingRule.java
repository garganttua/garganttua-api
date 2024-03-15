package com.garganttua.api.core.mapper.rules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public record GGAPIMappingRule (
		String sourceFieldAddress,
		String destinationFieldAddress,
		Field destinationField, 
		Class<?> destinationClass,
		Method fromSourceMethod,
		Method toSourceMethod
	){

    @Override
    public String toString() {
        return "GGAPIMappingRule{" +
                "sourceFieldAddress='" + sourceFieldAddress + '\'' +
                ", destinationFieldAddress='" + destinationFieldAddress + '\'' +
                ", destinationField=" + destinationField +
                ", destinationClass=" + destinationClass +
                ", fromSourceMethod=" + fromSourceMethod +
                ", toSourceMethod=" + toSourceMethod +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GGAPIMappingRule that = (GGAPIMappingRule) o;
        return Objects.equals(sourceFieldAddress, that.sourceFieldAddress) &&
                Objects.equals(destinationFieldAddress, that.destinationFieldAddress) &&
                Objects.equals(destinationField, that.destinationField) &&
                Objects.equals(destinationClass, that.destinationClass) &&
                Objects.equals(fromSourceMethod, that.fromSourceMethod) &&
                Objects.equals(toSourceMethod, that.toSourceMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFieldAddress, destinationFieldAddress, destinationField, destinationClass, fromSourceMethod, toSourceMethod);
    }

}
