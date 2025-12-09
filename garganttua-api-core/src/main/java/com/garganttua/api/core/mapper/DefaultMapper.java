package com.garganttua.api.core.mapper;

import com.garganttua.core.mapper.IMapper;
import com.garganttua.core.mapper.Mapper;

public class DefaultMapper {
    private static final IMapper DEFAULT_MAPPER;

    static {
        DEFAULT_MAPPER = new Mapper();
    }

    private DefaultMapper() {
    }

    public static IMapper mapper() {
        return DEFAULT_MAPPER;
    }
}
