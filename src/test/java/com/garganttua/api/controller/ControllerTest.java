package com.garganttua.api.controller;

import org.junit.jupiter.api.Test;

import com.garganttua.api.repository.dto.IGGAPIDTOFactory;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.GGAPIEntityException;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.IGGAPIEntityFactory;

import lombok.AllArgsConstructor;

class ControllerTest {
	
	private class TestDto implements IGGAPIDTOObject<TestEntity> {

		@Override
		public void create(TestEntity entity) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public TestEntity convert() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void update(IGGAPIDTOObject<TestEntity> object) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public IGGAPIDTOFactory<TestEntity, ? extends IGGAPIDTOObject<TestEntity>> getFactory() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getUuid() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getTenantId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	@AllArgsConstructor
	private class TestEntity implements IGGAPIEntity {
		
		private String test;
		private Integer integer;
		private int integer2;
		private Toto toto;

		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setId(String id) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getUuid() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setUuid(String uuid) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public IGGAPIEntityFactory<? extends IGGAPIEntity> getFactory() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Test
	void testMandatoryField() throws GGAPIEntityException {
		
		IGGAPIDomain domain = new IGGAPIDomain<TestEntity, TestDto>() {

			@Override
			public Class<TestEntity> getEntityClass() {
				return TestEntity.class;
			}

			@Override
			public Class<TestDto> getDtoClass() {
				return TestDto.class;
			}

			@Override
			public String getDomain() {
				return "test";
			}
			
		};
		
		GGAPIController controller = new GGAPIController<>(domain);
		
		String[] fields = {"test", "integer", "integer2", "toto"};
		
		controller.checkMandatoryFields(fields, new TestEntity("test", 12, -1, new Toto()));
		
	}

}
