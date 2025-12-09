package com.garganttua.api.spec.security;

import com.garganttua.api.spec.context.Access;

public record Security(boolean authority, Access access) {

}
