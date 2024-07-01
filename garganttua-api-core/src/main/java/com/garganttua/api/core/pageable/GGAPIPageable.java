package com.garganttua.api.core.pageable;

import com.garganttua.api.spec.pageable.IGGAPIPageable;

import lombok.Getter;

public class GGAPIPageable implements IGGAPIPageable {

	@Getter
	private int pageSize;
	@Getter
	private int pageIndex;

	private GGAPIPageable(int pageSize, int pageIndex) {
		this.pageSize = pageSize;
		this.pageIndex = pageIndex;
	}

	public static IGGAPIPageable getPage(int pageSize, int pageIndex) {
		return new GGAPIPageable(pageSize, pageIndex);
	}

}
