package com.garganttua.api.commons.filter;

import java.util.List;

public interface IFilter extends Cloneable {

	Object getValue();
	
	void setValue(Object value);

	IFilter clone();

	List<IFilter> getFilters();

	String getName();

	void setFilters(List<IFilter> valuesFilters);

	void removeSubFilter(IFilter filter);

	void replaceSubFilter(IFilter literal, IFilter mappedFilter);

}
