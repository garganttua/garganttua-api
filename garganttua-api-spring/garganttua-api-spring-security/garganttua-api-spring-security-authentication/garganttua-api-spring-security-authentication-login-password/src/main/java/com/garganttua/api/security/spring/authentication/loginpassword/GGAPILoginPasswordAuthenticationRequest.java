package com.garganttua.api.security.spring.authentication.loginpassword;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GGAPILoginPasswordAuthenticationRequest {
	
	private String login;
	
	private String password;

}
