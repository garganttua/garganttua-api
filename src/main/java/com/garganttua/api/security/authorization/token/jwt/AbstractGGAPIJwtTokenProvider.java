package com.garganttua.api.security.authorization.token.jwt;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;

import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.security.authentication.IGGAPIAuthenticator;
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
	public GGAPIToken getAuthorization(Authentication authentication) throws GGAPIKeyExpiredException, GGAPIEngineException {
		GGAPIToken token;
		try {

			token = this.generateToken((IGGAPIAuthenticator) authentication.getPrincipal());
			
		} catch (InvalidKeyException | SignatureException | InvalidKeySpecException | NoSuchAlgorithmException
				| GGAPIKeyExpiredException e) {
			throw new GGAPIEngineException(e);
		}
		return token;
	}

	private GGAPIToken generateToken(IGGAPIAuthenticator user) throws GGAPIKeyExpiredException, InvalidKeyException,
			SignatureException, InvalidKeySpecException, NoSuchAlgorithmException {
		GGAPIKey key = this.keyManager.getKeyForCiphering(this.keyRealm);
		Map<String, Object> claims = new HashMap<>();
		Date now = new Date();
		Date expiration = null;

		claims.put("tenantId", user.getTenantId());
		claims.put("ownerId", user.getUuid());

		if (user.getAuthorities() != null) {
			List<String> auths = new ArrayList<String>();
			user.getAuthorities().forEach(auth -> {
				auths.add(auth.getAuthority());
			});
			claims.put("authorizations", auths);
		}
		JwtBuilder token = Jwts.builder().setClaims(claims).setSubject(user.getAuthentication().getName()).setIssuedAt(now).signWith(key.getKey(), SignatureAlgorithm.forName(this.keyAlgorythm));

		if (this.tokenLifetime != 0) {
			long expirationDate = now.getTime() + TimeUnit.MINUTES.toMillis(this.tokenLifetime);
			expiration = new Date(expirationDate);
			token.setExpiration(expiration);
		}

		String tokenStr = token.compact();
		
		GGAPIToken tokenObj = new GGAPIToken(UUID.randomUUID().toString(), user.getUuid(), user.getTenantId(), now, expiration, tokenStr.getBytes(), key.getUuid(), user);
		
		this.storeToken(tokenObj);

		return tokenObj;
	}

	private <T> T extractClaim(byte[] token, Function<Claims, T> claimsResolver)
			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(byte[] token)
			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
		return Jwts.parserBuilder().setSigningKey(this.keyManager.getKeyForCiphering(this.keyRealm).getKey()).build()
				.parseClaimsJws(new String(token)).getBody();
	}

	public String extractUsername(byte[] token)
			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
		return extractClaim(token, Claims::getSubject);
	}

	private Boolean isTokenExpired(byte[] token)
			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
		return extractExpiration(token).before(new Date());
	}

	public Date extractExpiration(byte[] token)
			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
		return extractClaim(token, Claims::getExpiration);
	}

	@Override
	public GGAPIToken validateAuthorization(byte[] token)
			throws GGAPIKeyExpiredException, GGAPITokenNotFoundException, GGAPIEngineException, GGAPITokenExpired {
	
		try {
			Claims claims = extractAllClaims(token);
			
			String ownerId = (String) claims.get("ownerId");

			GGAPIToken tokenExample = new GGAPIToken(null, ownerId, null, null, null, token, null, null);
	
			GGAPIToken storeToken = this.findToken(tokenExample);
			
			boolean storeToken__ = (storeToken != null && new String(token).equals(new String(storeToken.getToken()))) || storeToken == null;
			
			boolean tokenValid = (storeToken.getOwnerId().equals(ownerId) && !(this.tokenLifetime!=0 && isTokenExpired(token)) && storeToken__);
			if( !tokenValid ) {
				return null;
			}
			
			return storeToken;
			
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
