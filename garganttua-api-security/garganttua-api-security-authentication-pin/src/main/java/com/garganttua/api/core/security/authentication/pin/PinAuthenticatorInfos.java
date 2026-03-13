package com.garganttua.api.core.security.authentication.pin;

import com.garganttua.core.reflection.ObjectAddress;

public record PinAuthenticatorInfos(ObjectAddress loginFieldAddress, ObjectAddress pinFieldAddress, ObjectAddress pinErrorCounterFieldAddress, int pinSize, int maxPinErrorNumber) {

}
