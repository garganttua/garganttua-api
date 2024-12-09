package com.garganttua.api.security.authentication.interfaces.spring.challenge.rest;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.garganttua.api.spec.security.authentication.GGAPIChallenge;
import com.garganttua.api.spec.security.authentication.GGAPIChallengeType;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GGAPIChallengeRestResponse {

	@Getter
	@JsonProperty
	private byte[] challenge;
	
	@Getter
	@JsonProperty
	private GGAPIChallengeType type;
	
	@Getter
	@JsonProperty
	private Date expiration;
	
	public GGAPIChallengeRestResponse(GGAPIChallenge challenge) {
		this.expiration = challenge.getExpiration();
		this.type = challenge.getType();
		this.challenge = challenge.getChallenge();
	}
}
