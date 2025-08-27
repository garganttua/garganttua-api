package com.garganttua.api.core.service;

import java.util.List;

public record Page(long totalCount, List<Object> entities) {

}
