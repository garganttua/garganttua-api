package com.garganttua.api.spec.entity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.javatuples.Pair;

import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.core.reflection.ObjectAddress;

public record EntityDefinition(
		String domain,
		ObjectAddress uuidFieldAddress,
		ObjectAddress idFieldAddress,
		boolean tenantEntity,
		boolean ownerEntity,
		boolean ownedEntity,
		ObjectAddress tenantIdFieldAddress,
		ObjectAddress superTenantFieldAddress,
		ObjectAddress ownerIdFieldAddress,
		ObjectAddress superOnwerIdFieldAddress,
		boolean publicEntity,
		boolean hiddenableEntity,
		ObjectAddress hiddenFieldAddress,
		boolean geolocalizedEntity,
		ObjectAddress locationFieldAddress,
		boolean sharedEntity,
		ObjectAddress shareFieldAddress,
		List<ObjectAddress> mandatoryFields,
		List<Pair<ObjectAddress, UnicityScope>> unicityFields,
		ObjectAddress afterGetMethodAddress,
		ObjectAddress beforeCreateMethodAddress,
		ObjectAddress afterCreateMethodAddress,
		ObjectAddress beforeUpdateMethodAddress,
		ObjectAddress afterUpdateMethodAddress,
		ObjectAddress beforeDeleteMethodAddress,
		ObjectAddress afterDeleteMethodAddress,
		Map<ObjectAddress, String> updateAuthorizations) {
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		EntityDefinition other = (EntityDefinition) obj;

		return Objects.equals(uuidFieldAddress, other.uuidFieldAddress) &&
				Objects.equals(domain, other.domain) &&
				Objects.equals(idFieldAddress, other.idFieldAddress) &&
				tenantEntity == other.tenantEntity &&
				ownerEntity == other.ownerEntity &&
				ownedEntity == other.ownedEntity &&
				Objects.equals(tenantIdFieldAddress, other.tenantIdFieldAddress) &&
				Objects.equals(superTenantFieldAddress, other.superTenantFieldAddress) &&
				Objects.equals(ownerIdFieldAddress, other.ownerIdFieldAddress) &&
				Objects.equals(superOnwerIdFieldAddress, other.superOnwerIdFieldAddress) &&
				publicEntity == other.publicEntity &&
				hiddenableEntity == other.hiddenableEntity &&
				Objects.equals(hiddenFieldAddress, other.hiddenFieldAddress) &&
				geolocalizedEntity == other.geolocalizedEntity &&
				Objects.equals(locationFieldAddress, other.locationFieldAddress) &&
				sharedEntity == other.sharedEntity &&
				Objects.equals(shareFieldAddress, other.shareFieldAddress) &&
				Objects.equals(mandatoryFields, other.mandatoryFields) &&
				Objects.equals(unicityFields, other.unicityFields) &&
				Objects.equals(afterGetMethodAddress, other.afterGetMethodAddress) &&
				Objects.equals(beforeCreateMethodAddress, other.beforeCreateMethodAddress) &&
				Objects.equals(afterCreateMethodAddress, other.afterCreateMethodAddress) &&
				Objects.equals(beforeUpdateMethodAddress, other.beforeUpdateMethodAddress) &&
				Objects.equals(afterUpdateMethodAddress, other.afterUpdateMethodAddress) &&
				Objects.equals(beforeDeleteMethodAddress, other.beforeDeleteMethodAddress) &&
				Objects.equals(afterDeleteMethodAddress, other.afterDeleteMethodAddress) &&
				Objects.equals(updateAuthorizations, other.updateAuthorizations);
	}
}