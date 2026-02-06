package com.garganttua.api.core.examples;

import com.garganttua.api.spec.entity.annotations.Entity;
import com.garganttua.api.spec.entity.annotations.EntityId;
import com.garganttua.api.spec.entity.annotations.EntityUuid;
import com.garganttua.api.spec.entity.annotations.EntityTenantId;
import com.garganttua.api.spec.entity.annotations.EntityOwnerId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Example entity demonstrating the garganttua-api entity annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(
    domain = "users",
    interfaces = {"rest"},
    allow_creation = true,
    allow_read_all = true,
    allow_read_one = true,
    allow_update_one = true,
    allow_delete_one = true,
    allow_delete_all = false
)
public class User {

    @EntityId
    private String id;

    @EntityUuid
    private String uuid;

    @EntityTenantId
    private String tenantId;

    @EntityOwnerId
    private String ownerId;

    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;

}
