package com.garganttua.api.spec.service;

import java.util.List;

public record Page(long totalCount, List<Object> entities) {

}
