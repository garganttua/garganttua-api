package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.service.Access;

public record Security(boolean authority, Access access) {

}
