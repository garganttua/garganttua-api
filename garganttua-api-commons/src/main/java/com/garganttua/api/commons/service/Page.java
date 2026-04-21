package com.garganttua.api.commons.service;

import java.util.List;

public record Page(long totalCount, List<Object> entities) {

}
