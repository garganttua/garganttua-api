package com.garganttua.api.spec.entity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.javatuples.Pair;

import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.reflection.GGObjectAddress;

public record EntityInfos (
		String domain,
        GGObjectAddress uuidFieldAddress,
        GGObjectAddress idFieldAddress,
        GGObjectAddress saveProviderFieldAddress,
        GGObjectAddress deleteProviderFieldAddress,
        boolean tenantEntity,
        boolean ownerEntity,
        boolean ownedEntity,
        GGObjectAddress tenantIdFieldAddress,
        GGObjectAddress superTenantFieldAddress,
        GGObjectAddress ownerIdFieldAddress,
        GGObjectAddress superOnwerIdFieldAddress,
        GGObjectAddress saveMethodAddress,
        GGObjectAddress deleteMethodAddress,
        boolean publicEntity,
        boolean hiddenableEntity,
        GGObjectAddress hiddenFieldAddress,
        boolean geolocalizedEntity,
        GGObjectAddress locationFieldAddress,
        boolean sharedEntity,
        GGObjectAddress shareFieldAddress,
        GGObjectAddress repositoryFieldAddress,
        GGObjectAddress engineFieldAddress,
        List<GGObjectAddress> mandatoryFields,
        List<Pair<GGObjectAddress, UnicityScope>> unicityFields,
        GGObjectAddress afterGetMethodAddress,
        GGObjectAddress beforeCreateMethodAddress, 
        GGObjectAddress afterCreateMethodAddress, 
        GGObjectAddress beforeUpdateMethodAddress, 
        GGObjectAddress afterUpdateMethodAddress, 
        GGObjectAddress beforeDeleteMethodAddress, 
        GGObjectAddress afterDeleteMethodAddress, 
        Map<GGObjectAddress, String> updateAuthorizations,
        GGObjectAddress gotFromRepositoryFieldAddress
) {
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null || getClass() != obj.getClass()) return false;

	    EntityInfos other = (EntityInfos) obj;

	    return Objects.equals(uuidFieldAddress, other.uuidFieldAddress) &&
	    		Objects.equals(domain, other.domain) &&
	            Objects.equals(idFieldAddress, other.idFieldAddress) &&
	            Objects.equals(saveProviderFieldAddress, other.saveProviderFieldAddress) &&
	            Objects.equals(deleteProviderFieldAddress, other.deleteProviderFieldAddress) &&
	            tenantEntity == other.tenantEntity &&
	            ownerEntity == other.ownerEntity &&
	            ownedEntity == other.ownedEntity &&
	            Objects.equals(tenantIdFieldAddress, other.tenantIdFieldAddress) &&
	            Objects.equals(superTenantFieldAddress, other.superTenantFieldAddress) &&
	            Objects.equals(ownerIdFieldAddress, other.ownerIdFieldAddress) &&
	            Objects.equals(superOnwerIdFieldAddress, other.superOnwerIdFieldAddress) &&
	            Objects.equals(saveMethodAddress, other.saveMethodAddress) &&
	            Objects.equals(deleteMethodAddress, other.deleteMethodAddress) &&
	            publicEntity == other.publicEntity &&
	            hiddenableEntity == other.hiddenableEntity &&
	            Objects.equals(hiddenFieldAddress, other.hiddenFieldAddress) &&
	            geolocalizedEntity == other.geolocalizedEntity &&
	            Objects.equals(locationFieldAddress, other.locationFieldAddress) &&
	            sharedEntity == other.sharedEntity &&
	            Objects.equals(shareFieldAddress, other.shareFieldAddress) &&
	            Objects.equals(repositoryFieldAddress, other.repositoryFieldAddress) &&
	            Objects.equals(engineFieldAddress, other.engineFieldAddress) &&
	            Objects.equals(mandatoryFields, other.mandatoryFields) &&
	            Objects.equals(unicityFields, other.unicityFields) &&
	            Objects.equals(afterGetMethodAddress, other.afterGetMethodAddress) &&
	            Objects.equals(beforeCreateMethodAddress, other.beforeCreateMethodAddress) &&
	            Objects.equals(afterCreateMethodAddress, other.afterCreateMethodAddress) &&
	            Objects.equals(beforeUpdateMethodAddress, other.beforeUpdateMethodAddress) &&
	            Objects.equals(afterUpdateMethodAddress, other.afterUpdateMethodAddress) &&
	            Objects.equals(beforeDeleteMethodAddress, other.beforeDeleteMethodAddress) &&
	            Objects.equals(afterDeleteMethodAddress, other.afterDeleteMethodAddress) &&
	            Objects.equals(updateAuthorizations, other.updateAuthorizations) &&
	            Objects.equals(gotFromRepositoryFieldAddress, other.gotFromRepositoryFieldAddress);
	}
}