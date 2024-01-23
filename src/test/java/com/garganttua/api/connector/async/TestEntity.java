package com.garganttua.api.connector.async;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.IGGAPIEntityFactory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestEntity implements IGGAPIEntity {
	
	@JsonProperty
	private String uuid;
	
	@JsonProperty
	private String id;

	@Override
	public IGGAPIEntityFactory<TestEntity> getFactory() {
		return new IGGAPIEntityFactory<TestEntity>() {

			@Override
			public TestEntity newInstance() {
				TestEntity entity = new TestEntity();
				entity.setUuid(UUID.randomUUID().toString());
				return entity;
			}

			@Override
			public TestEntity newInstance(String uuid) {
				TestEntity entity = new TestEntity();
				entity.setUuid(uuid);
				return entity;
			}
		};
	}
}
