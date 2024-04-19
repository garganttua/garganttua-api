package com.garganttua.api.security.keys;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.mapper.GGAPIMapper;
import com.garganttua.api.core.mapper.GGAPIMapperConfigurationItem;
import com.garganttua.api.core.mapper.GGAPIMapperException;
import com.garganttua.api.core.security.keys.GGAPIKey;
import com.garganttua.api.core.security.keys.GGAPIKeyRealmDTO;
import com.garganttua.api.core.security.keys.GGAPIKeyRealmEntity;
import com.garganttua.api.core.security.keys.GGAPIKeyType;

public class GGAPIKeyRealmMappingTest {

	@Test
	public void testNormalMapping() throws GGAPIMapperException {
		Date expirationDate = new Date();
		GGAPIKey cipheringKey = new GGAPIKey("uuid", "algo", expirationDate , GGAPIKeyType.SYMETRIC, "key".getBytes());
		
		GGAPIKeyRealmEntity realm = new GGAPIKeyRealmEntity("uuid", "id", "algo", cipheringKey, cipheringKey);
	
		GGAPIMapper mapper = new GGAPIMapper();
		mapper.configure(GGAPIMapperConfigurationItem.FAIL_ON_ERROR, false);
		GGAPIKeyRealmDTO dto = mapper.map(realm, GGAPIKeyRealmDTO.class);
		
		assertNotNull(dto);
		
	}
}
