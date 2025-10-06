package com.garganttua.api.core.pageable;

import com.garganttua.api.spec.pageable.IPageable;

import lombok.Getter;

public class Pageable implements IPageable {

	@Getter
	private int pageSize;
	@Getter
	private int pageIndex;

	private Pageable(int pageSize, int pageIndex) {
		this.pageSize = pageSize;
		this.pageIndex = pageIndex;
	}

	public static IPageable getPage(int pageSize, int pageIndex) {
		return new Pageable(pageSize, pageIndex);
	}

}
