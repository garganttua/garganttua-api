package com.garganttua.api.spec.engine;

import com.garganttua.reflection.GGObjectAddress;

public record Business(
    Boolean publik,
    GGObjectAddress owner,
    GGObjectAddress owned,
    GGObjectAddress shared,
    GGObjectAddress hiddenable) {

}
