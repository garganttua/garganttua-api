package com.garganttua.api.commons.pageable;

/**
 * Plain {@link IPageable} carrier — a 0-based page index and a page size. Used by the DSL
 * and the transport layer (which builds it from {@code ?page=&size=}).
 */
public class Pageable implements IPageable {

	private final int pageIndex;
	private final int pageSize;

	public Pageable(int pageIndex, int pageSize) {
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}

	@Override
	public int getPageIndex() {
		return this.pageIndex;
	}

	@Override
	public int getPageSize() {
		return this.pageSize;
	}
}
