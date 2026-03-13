package com.garganttua.api.core.security.authentication.loginpassword;

import com.garganttua.core.reflection.ObjectAddress;

public record LoginPasswordAuthenticatorInfos(ObjectAddress loginFieldAddress, ObjectAddress passwordFieldAddress) {

}
