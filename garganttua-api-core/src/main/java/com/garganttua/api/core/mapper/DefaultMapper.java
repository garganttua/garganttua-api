package com.garganttua.api.core.mapper;

import com.garganttua.core.mapper.IMapper;
import com.garganttua.core.mapper.Mapper;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;

public class DefaultMapper {

    private static final IReflection REFLECTION;
    private static final IMapper DEFAULT_MAPPER;

    static {
        REFLECTION = ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .build();
        DEFAULT_MAPPER = new Mapper(REFLECTION);
    }

    private DefaultMapper() {
    }

    public static IMapper mapper() {
        return DEFAULT_MAPPER;
    }

    public static IReflection reflection() {
        return REFLECTION;
    }
}
