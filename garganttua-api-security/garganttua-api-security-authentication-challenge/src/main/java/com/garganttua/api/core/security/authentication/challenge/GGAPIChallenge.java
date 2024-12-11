package com.garganttua.api.spec.security.authentication;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class GGAPIChallenge {

	@Getter
	@Setter
	private byte[] challenge;
	
	@Getter
	@Setter
	private GGAPIChallengeType type;
	
	@Getter
	@Setter
	private Date expiration;
}
