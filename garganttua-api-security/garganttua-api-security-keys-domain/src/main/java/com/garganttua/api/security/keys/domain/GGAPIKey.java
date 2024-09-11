package com.garganttua.api.security.keys.domain;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class GGAPIKey {
	
	private byte[] key;

	@Override
	public boolean equals(Object obj) {
		return Arrays.equals(key, ((GGAPIKey) obj).key);
	}
}
