package com.garganttua.api.core.security.authentication.pin;

import com.garganttua.reflection.GGObjectAddress;

public record PinAuthenticatorInfos(GGObjectAddress loginFieldAddress, GGObjectAddress pinFieldAddress, GGObjectAddress pinErrorCounterFieldAddress, int pinSize, int maxPinErrorNumber) {

}
