package com.garganttua.api.core.mapper;

import com.garganttua.objects.mapper.GGMapper;

public class DefaultMapper {
	private static final GGMapper DEFAULT_MAPPER;

    static {
    	DEFAULT_MAPPER = new GGMapper();
    }

    private DefaultMapper() {
    }

    public static GGMapper mapper() {
        return DEFAULT_MAPPER;
    }
}
