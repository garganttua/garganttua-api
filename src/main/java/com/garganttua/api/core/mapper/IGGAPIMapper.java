package com.garganttua.api.core.mapper;

public interface IGGAPIMapper {
	
	GGAPIMapper configure(GGAPIMapperConfigurationItem element, Object value);

	<destination> destination map(Object source, Class<destination> destinationClass) throws GGAPIMapperException;

}
