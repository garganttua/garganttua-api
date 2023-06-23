package com.garganttua.api.security.authorization.token.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import com.garganttua.api.security.authentication.dao.AbstractGGAPIUserDetails;
import com.garganttua.api.security.authorization.IGGAPIAuthorizationProvider;
import com.garganttua.api.security.keys.IGGAPIKeyManager;
import com.garganttua.api.security.keys.GGAPIInMemoryKeyManager;
import com.garganttua.api.security.keys.GGAPIKeyExpiration;
import com.garganttua.api.security.keys.GGAPIKeyExpiredException;
import com.garganttua.api.security.keys.GGAPIKeyManagerType;
import com.garganttua.api.security.keys.GGAPIKeyRenewal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;

public class GGAPIJwtTokenProvider implements IGGAPIAuthorizationProvider {

	private IGGAPIKeyManager keyManager;

	@Value("${spring.domain.crudify.security.authorization.token.jwt.key.manager}")
	private GGAPIKeyManagerType keyManagerType;

	@Value("${spring.domain.crudify.security.authorization.token.jwt.key.algorithm}")
	private String keyAlgorythm;

	@Value("${spring.domain.crudify.security.authorization.token.jwt.key.realm}")
	private String keyRealm;

	@Value("${spring.domain.crudify.security.authorization.token.jwt.key.renewal}")
	private GGAPIKeyRenewal keyRenewal;

	@Value("${spring.domain.crudify.security.authorization.token.jwt.key.lifetime}")
	private long keyLifetime;

	@Value("${spring.domain.crudify.security.authorization.token.jwt.key.lifetime.unit}")
	private TimeUnit keyLifetimeUnit;

	@Value("${spring.domain.crudify.security.authorization.token.lifetime}")
	private int tokenLifetime;

	@PostConstruct
	private void init() {
		switch (this.keyManagerType) {
		default:
		case inmemory:
			this.keyManager = new GGAPIInMemoryKeyManager();
			break;
		case db:
			break;
		}

		if (this.keyLifetime == 0) {
			this.keyManager.createRealm(this.keyRealm, SignatureAlgorithm.forName(this.keyAlgorythm));
		} else {
			GGAPIKeyExpiration expiration = new GGAPIKeyExpiration(this.keyLifetime,
					this.keyLifetimeUnit);
			this.keyManager.createRealm(this.keyRealm, SignatureAlgorithm.forName(this.keyAlgorythm), expiration);
		}
	}

	@Override
	public String getAuthorization(Authentication authentication) throws GGAPIKeyExpiredException {
		AbstractGGAPIUserDetails principal = (AbstractGGAPIUserDetails) authentication.getPrincipal();

		return this.generateToken(principal.getUsername(), principal.getTenantId(), principal.getUuid());
	}

	public String generateToken(String userName, String tenantId, String uuid) throws GGAPIKeyExpiredException {
		Map<String, Object> claims = new HashMap<>();
		claims.put("tenantId", tenantId);
		claims.put("uuid", uuid);
		return createToken(claims, userName);
	}

	private String createToken(Map<String, Object> claims, String userName) throws GGAPIKeyExpiredException {
		Key key = this.keyManager.getKeyForCiphering(this.keyRealm);

		Date now = new Date();

		JwtBuilder token = Jwts.builder().setClaims(claims).setSubject(userName).setIssuedAt(now).signWith(key,
				SignatureAlgorithm.forSigningKey(key));

		if (this.tokenLifetime != 0) {
			long expirationDate = now.getTime() + TimeUnit.MINUTES.toMillis(this.tokenLifetime);
			token.setExpiration(new Date(expirationDate));
		}
		return token.compact();
	}

	@Override
	public String getUserNameFromAuthorization(String token) throws GGAPIKeyExpiredException {
		return extractClaim(token, Claims::getSubject);
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
			throws GGAPIKeyExpiredException {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) throws GGAPIKeyExpiredException {
		return Jwts.parserBuilder().setSigningKey(this.keyManager.getKeyForCiphering(this.keyRealm)).build()
				.parseClaimsJws(token).getBody();
	}

	public String extractUsername(String token) throws GGAPIKeyExpiredException {
		return extractClaim(token, Claims::getSubject);
	}

	private Boolean isTokenExpired(String token) throws GGAPIKeyExpiredException {
		return extractExpiration(token).before(new Date());
	}

	public Date extractExpiration(String token) throws GGAPIKeyExpiredException {
		return extractClaim(token, Claims::getExpiration);
	}

	@Override
	public boolean validateAuthorization(String token, UserDetails userDetails) throws GGAPIKeyExpiredException {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

}
