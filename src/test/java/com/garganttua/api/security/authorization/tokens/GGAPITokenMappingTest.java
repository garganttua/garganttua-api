package com.garganttua.api.security.authorization.tokens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.mapper.GGAPIMapper;
import com.garganttua.api.core.mapper.GGAPIMapperException;
import com.garganttua.api.core.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.core.security.authorization.tokens.storage.GGAPITokenDTO;

public class GGAPITokenMappingTest {
	
	@Test
	public void testNormalMapping() throws GGAPIMapperException {
		
		Date creationDate = new Date();
		Date expirationDate = new Date();
		List<String> authorities = List.of("authority-1");
		
		GGAPIToken token = new GGAPIToken("tenantId", "uuid", "ownerId", creationDate, expirationDate, authorities , new String("token").getBytes(), "signingKeyId");
	
		GGAPIMapper mapper = new GGAPIMapper();
		GGAPITokenDTO dto = mapper.map(token, GGAPITokenDTO.class);
		
		assertNotNull(dto);
		assertEquals(token.getTenantId(), dto.getTenantId());
		assertEquals(token.getUuid(), dto.getUuid());
		assertEquals(token.getAuthorities(), dto.getAuthorities());
		assertEquals(token.getCreationDate(), dto.getCreationDate());
		assertEquals(token.getExpirationDate(), dto.getExpirationDate());
		assertEquals(token.getToken(), dto.getToken());
	
	}

}
