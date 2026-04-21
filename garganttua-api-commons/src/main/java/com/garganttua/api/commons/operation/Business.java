package com.garganttua.api.commons.operation;

import com.garganttua.core.reflection.ObjectAddress;

public record Business(
        Boolean publik,
        ObjectAddress owner,
        ObjectAddress owned,
        ObjectAddress shared,
        ObjectAddress hiddenable) {

}
