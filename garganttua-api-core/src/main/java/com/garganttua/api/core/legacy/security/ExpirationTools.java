package com.garganttua.api.core.legacy.security;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ExpirationTools {
	
	public static Date getExpirationDateFromNow(int lifeTime, TimeUnit timeUnit) {
		return Date
				.from(Instant.ofEpochSecond(Instant.now().getEpochSecond() + timeUnit.toSeconds(lifeTime)));
	}

}
