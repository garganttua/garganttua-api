package com.garganttua.api.core.examples;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class for User entity, used for data transfer operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String id;
    private String uuid;
    private String tenantId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;

}
