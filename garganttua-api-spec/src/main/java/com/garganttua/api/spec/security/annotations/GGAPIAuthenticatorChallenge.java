package com.garganttua.api.spec.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.security.authentication.GGAPIChallengeType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GGAPIAuthenticatorChallenge {

	GGAPIChallengeType challengeType() default GGAPIChallengeType.TIME_LIMITED;

	TimeUnit challengeLifeTimeUnit() default TimeUnit.MINUTES;

	int challengeLifeTime() default 1;

}
