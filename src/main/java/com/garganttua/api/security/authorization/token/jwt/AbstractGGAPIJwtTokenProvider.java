package com.garganttua.api.security.authorization.token.jwt;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;

import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.security.authentication.dao.AbstractGGAPIUserDetails;
import com.garganttua.api.security.authorization.IGGAPIAuthorizationProvider;
import com.garganttua.api.security.authorization.token.GGAPIToken;
import com.garganttua.api.security.keys.GGAPIKey;
import com.garganttua.api.security.keys.GGAPIKeyExpiration;
import com.garganttua.api.security.keys.GGAPIKeyExpiredException;
import com.garganttua.api.security.keys.GGAPIKeyRealms;
import com.garganttua.api.security.keys.GGAPIKeyRenewal;
import com.garganttua.api.security.keys.IGGAPIKeyManager;
import com.garganttua.api.security.keys.IGGAPIKeyRealm;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;

public abstract class AbstractGGAPIJwtTokenProvider implements IGGAPIAuthorizationProvider {

	@Autowired
	private IGGAPIKeyManager keyManager;

	@Value("${com.garganttua.api.security.authorization.token.lifetime}")
	private int tokenLifetime;
	
	@Value("${com.garganttua.api.security.authorization.token.jwt.key.algorithm}")
	private String keyAlgorythm;

	@Value("${com.garganttua.api.security.authorization.token.jwt.key.realm}")
	private String keyRealm;

	@Value("${com.garganttua.api.security.authorization.token.jwt.key.renewal}")
	private GGAPIKeyRenewal keyRenewal;

	@Value("${com.garganttua.api.security.authorization.token.jwt.key.lifetime}")
	private long keyLifetime;

	@Value("${com.garganttua.api.security.authorization.token.jwt.key.lifetime.unit}")
	private TimeUnit keyLifetimeUnit;
	
	@Value("${com.garganttua.api.security.authorization.token.jwt.key.realm.create}")
	private boolean createRealm;
	
	@PostConstruct
	private IGGAPIKeyManager keyManager() {
		
		GGAPIKeyExpiration expiration = null;
		
		if (this.keyLifetime > 0) {
			expiration = new GGAPIKeyExpiration(this.keyLifetime, this.keyLifetimeUnit);
		}
		IGGAPIKeyRealm realm = GGAPIKeyRealms.createRealm(this.keyRealm, SignatureAlgorithm.forName(this.keyAlgorythm).getValue(), expiration);
		
		try {
			if( this.keyManager.getKeyForCiphering(this.keyRealm) == null && this.createRealm ) {
				this.keyManager.createRealm(realm);
			}
		} catch (GGAPIKeyExpiredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return keyManager;
	}

	@Override
	public GGAPIToken getAuthorization(AbstractGGAPIUserDetails userDetails)
			throws GGAPIKeyExpiredException, GGAPIEngineException {
		GGAPIToken token;
		try {
			token = this.generateToken(userDetails.getUsername(), userDetails.getTenantId(), userDetails.getUuid(),
					userDetails.getAuthorities());
			
			
		} catch (InvalidKeyException | SignatureException | InvalidKeySpecException | NoSuchAlgorithmException
				| GGAPIKeyExpiredException e) {
			throw new GGAPIEngineException(e);
		}
		return token;
	}

	private GGAPIToken generateToken(String userName, String tenantId, String uuid, Collection<? extends GrantedAuthority> authorizations) throws GGAPIKeyExpiredException, InvalidKeyException,
			SignatureException, InvalidKeySpecException, NoSuchAlgorithmException {
		GGAPIKey key = this.keyManager.getKeyForCiphering(this.keyRealm);
		Map<String, Object> claims = new HashMap<>();
		Date now = new Date();
		Date expiration = null;

		claims.put("tenantId", tenantId);
		claims.put("uuid", uuid);

		if (authorizations != null) {
			List<String> auths = new ArrayList<String>();
			authorizations.forEach(auth -> {
				auths.add(auth.getAuthority());
			});
			claims.put("authorizations", auths);
		}
		JwtBuilder token = Jwts.builder().setClaims(claims).setSubject(userName).setIssuedAt(now).signWith(key.getKey(), SignatureAlgorithm.forName(this.keyAlgorythm));

		if (this.tokenLifetime != 0) {
			long expirationDate = now.getTime() + TimeUnit.MINUTES.toMillis(this.tokenLifetime);
			expiration = new Date(expirationDate);
			token.setExpiration(expiration);
		}

		String tokenStr = token.compact();
		
		GGAPIToken tokenObj = new GGAPIToken(UUID.randomUUID().toString(), uuid, now, expiration, tokenStr.getBytes(), key.getUuid());
		
		this.storeToken(tokenObj);

		return new GGAPIToken(UUID.randomUUID().toString(), uuid, now, expiration, tokenStr.getBytes(), key.getUuid());
	}

	@Override
	public String getUserNameFromAuthorization(String token) throws GGAPIKeyExpiredException, GGAPIEngineException {
		try {
			return extractClaim(token, Claims::getSubject);
		} catch (SignatureException | ExpiredJwtException | UnsupportedJwtException | MalformedJwtException
				| IllegalArgumentException | InvalidKeySpecException | NoSuchAlgorithmException
				| GGAPIKeyExpiredException e) {
			throw new GGAPIEngineException(e);
		}
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token)
			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
		return Jwts.parserBuilder().setSigningKey(this.keyManager.getKeyForCiphering(this.keyRealm).getKey()).build()
				.parseClaimsJws(token).getBody();
	}

	public String extractUsername(String token)
			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
		return extractClaim(token, Claims::getSubject);
	}

	private Boolean isTokenExpired(String token)
			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
		return extractExpiration(token).before(new Date());
	}

	public Date extractExpiration(String token)
			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
		return extractClaim(token, Claims::getExpiration);
	}

	@Override
	public boolean validateAuthorization(String token, AbstractGGAPIUserDetails userDetails)
			throws GGAPIKeyExpiredException, GGAPITokenNotFoundException, GGAPIEngineException, GGAPITokenExpired {
		String username;
		try {
			username = extractUsername(token);
		} catch (SignatureException | ExpiredJwtException | UnsupportedJwtException | MalformedJwtException
				| IllegalArgumentException | InvalidKeySpecException | NoSuchAlgorithmException
				| GGAPIKeyExpiredException e) {
			throw new GGAPIEngineException(e);
		}
		
		GGAPIToken tokenExample = new GGAPIToken(null, userDetails.getUuid(), null, null, token.getBytes(), null);

		GGAPIToken storeToken = this.findToken(tokenExample);

		boolean storeToken__ = (storeToken != null && token.equals(new String(storeToken.getToken())))
				|| storeToken == null;

		try {
			boolean tokenValid = (username.equals(userDetails.getUsername()) && !(this.tokenLifetime!=0 && isTokenExpired(token)) && storeToken__);
			return tokenValid;
		} catch (GGAPIKeyExpiredException e) {
			throw new GGAPIEngineException(e);
		} catch (SignatureException e) {
			throw new GGAPIEngineException(e);
		} catch (ExpiredJwtException e) {
			throw new GGAPITokenExpired(e);
		} catch (UnsupportedJwtException e) {
			throw new GGAPIEngineException(e);
		} catch (MalformedJwtException e) {
			throw new GGAPIEngineException(e);
		} catch (IllegalArgumentException e) {
			throw new GGAPIEngineException(e);
		} catch (InvalidKeySpecException e) {
			throw new GGAPIEngineException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new GGAPIEngineException(e);
		}
	}

	protected abstract void storeToken(GGAPIToken token);

	protected abstract GGAPIToken findToken(GGAPIToken token) throws GGAPITokenNotFoundException;

}
