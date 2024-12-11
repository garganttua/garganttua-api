package com.garganttua.api.core.security.authentication.challenge;

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
