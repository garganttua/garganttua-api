package com.garganttua.api.spec.security;

import com.garganttua.api.spec.operation.Access;

public record Security(boolean authority, Access access) {

}
