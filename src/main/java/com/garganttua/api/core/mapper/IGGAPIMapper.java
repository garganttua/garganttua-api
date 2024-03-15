package com.garganttua.api.core.mapper;

public interface IGGAPIMapper {

	<destination> destination map(Object source, Class<destination> destinationClass) throws GGAPIMapperException;

}
