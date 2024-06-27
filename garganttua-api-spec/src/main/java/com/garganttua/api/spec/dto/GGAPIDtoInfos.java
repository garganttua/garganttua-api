package com.garganttua.api.spec.dto;

import java.util.Objects;

import com.garganttua.reflection.GGObjectAddress;

public record GGAPIDtoInfos(String db, GGObjectAddress tenantIdFieldAddress) {
	@Override
	public String toString() {
		return "GGAPIDtoInfos{tenantIdFieldName='" + tenantIdFieldAddress + "', db='" + db + "'}";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GGAPIDtoInfos that = (GGAPIDtoInfos) o;
		return Objects.equals(tenantIdFieldAddress, that.tenantIdFieldAddress) &&
				Objects.equals(db, that.db);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tenantIdFieldAddress)*Objects.hash(db);
	}
}
