package com.garganttua.api.core.filter.mapper;

import java.util.List;

import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.dto.GenericGGAPIDto;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker.GGAPIEntityInfos;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.filter.GGAPILiteral;

@GGAPIEntity(
	domain = "test", 
	dto = { Dto.class }
)
class Entity extends GenericGGAPIEntity {
	
}

class Dto extends GenericGGAPIDto {
	
}

public class GGAPIFilterMapperTest {
	
	private static GGAPIFilterMapper mapper;

	@BeforeAll
	public static void init() {
		GGAPIFilterMapperTest.mapper = new GGAPIFilterMapper();
	}
	
	@Test
	public void test() throws GGAPIEntityException, GGAPILiteralMapperException {
		Entity entity = new Entity();
		entity.setUuid("uuid");
		entity.setId("id");
		GGAPIEntityInfos infos = new GGAPIEntityChecker().checkEntityClass(entity);
		
		GGAPILiteral filter = GGAPILiteral.eq(infos.uuidFieldName(), "uuid");
		
		List<Pair<Class<?>, GGAPILiteral>> filters = GGAPIFilterMapperTest.mapper.map(infos, filter);

	}

}
