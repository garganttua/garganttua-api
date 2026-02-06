package com.garganttua.api.spec.dto;

import java.util.Objects;

import com.garganttua.core.reflection.ObjectAddress;

public record DtoInfos(String db, ObjectAddress tenantIdFieldAddress) {
	
	@Override
	public String toString() {
		return "DtoInfos{tenantIdFieldName='" + tenantIdFieldAddress + "', db='" + db + "'}";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DtoInfos that = (DtoInfos) o;
		return Objects.equals(tenantIdFieldAddress, that.tenantIdFieldAddress) &&
				Objects.equals(db, that.db);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tenantIdFieldAddress)*Objects.hash(db);
	}
}
