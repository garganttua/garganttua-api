package com.garganttua.api.commons.security;

import com.garganttua.api.commons.operation.Access;

public record Security(boolean authority, Access access) {

}
