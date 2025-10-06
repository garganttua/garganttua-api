package com.garganttua.api.spec.entity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.javatuples.Pair;

import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.reflection.GGObjectAddress;

public record EntityDefinition(
		String domain,
		GGObjectAddress uuidFieldAddress,
		GGObjectAddress idFieldAddress,
		boolean tenantEntity,
		boolean ownerEntity,
		boolean ownedEntity,
		GGObjectAddress tenantIdFieldAddress,
		GGObjectAddress superTenantFieldAddress,
		GGObjectAddress ownerIdFieldAddress,
		GGObjectAddress superOnwerIdFieldAddress,
		boolean publicEntity,
		boolean hiddenableEntity,
		GGObjectAddress hiddenFieldAddress,
		boolean geolocalizedEntity,
		GGObjectAddress locationFieldAddress,
		boolean sharedEntity,
		GGObjectAddress shareFieldAddress,
		List<GGObjectAddress> mandatoryFields,
		List<Pair<GGObjectAddress, UnicityScope>> unicityFields,
		GGObjectAddress afterGetMethodAddress,
		GGObjectAddress beforeCreateMethodAddress,
		GGObjectAddress afterCreateMethodAddress,
		GGObjectAddress beforeUpdateMethodAddress,
		GGObjectAddress afterUpdateMethodAddress,
		GGObjectAddress beforeDeleteMethodAddress,
		GGObjectAddress afterDeleteMethodAddress,
		Map<GGObjectAddress, String> updateAuthorizations) {
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