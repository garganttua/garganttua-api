package com.garganttua.api.core.updater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
class Entity {
	
	String string;
	
	float floatt;
	
	int integer;
	
	public Entity() {
		
	}
}

public class GGAPIEntityUpdaterTest {
	
	private GGAPIEntityUpdater updater = new GGAPIEntityUpdater();

	@Test
	public void test() throws GGAPIException, GGReflectionException {
		IGGObjectQuery query = GGObjectQueryFactory.objectQuery(Entity.class);
		
		Entity storedEntity = new Entity("1", 1, 1);

		Entity newEntity = new Entity("2", 2, 2);

		Map<GGObjectAddress, String> updateAuthorizations = new HashMap<GGObjectAddress, String>();
		updateAuthorizations.put(query.address("string"), null);

		Entity updated = (Entity) this.updater.update(GGAPICaller.createSuperCaller(), storedEntity, newEntity, updateAuthorizations );
		
		assertNotNull(updated);
		assertEquals("2", updated.string);
		assertEquals(1, updated.integer);
		assertEquals(1, updated.floatt);
	}
	
	@Test
	public void testUpdateAuthorizationsNull() {
		
		Entity storedEntity = new Entity("1", 1, 1);
		Entity newEntity = new Entity("2", 2, 2);

		GGAPIException exception = assertThrows(GGAPIException.class, () -> { this.updater.update(null, storedEntity, newEntity, null);} );
		
		assertEquals("Update authorizations map is null", exception.getMessage());
		assertEquals(GGAPIExceptionCode.UNKNOWN_ERROR, exception.getCode());
	}
	
	@Test
	public void testCallerIsNull() {
		
		Entity storedEntity = new Entity("1", 1, 1);
		Entity newEntity = new Entity("2", 2, 2);

		GGAPIException exception = assertThrows(GGAPIException.class, () -> { this.updater.update(null, storedEntity, newEntity, new HashMap<GGObjectAddress, String>());} );
		
		assertEquals("Caller is null", exception.getMessage());
		assertEquals(GGAPIExceptionCode.UNKNOWN_ERROR, exception.getCode());
	}
	
	@Test
	public void testEntitiesNotSameType() {
		
		Entity storedEntity = new Entity("1", 1, 1);

		Integer newEntity = 12;

		GGAPIException exception = assertThrows(GGAPIException.class, () -> { this.updater.update(GGAPICaller.createSuperCaller(), storedEntity, newEntity, new HashMap<GGObjectAddress, String>());} );
		
		assertEquals("Stored entity type [Entity] and updated entity type [Integer] mismatch", exception.getMessage());
		assertEquals(GGAPIExceptionCode.UNKNOWN_ERROR, exception.getCode());
	}

}
