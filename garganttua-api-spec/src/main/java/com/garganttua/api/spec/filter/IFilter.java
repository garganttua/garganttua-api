package com.garganttua.api.spec.filter;

import java.util.List;

public interface IFilter extends Cloneable {

	Object getValue();
	
	void setValue(Object value);

	IFilter clone();

	List<IFilter> getLiterals();

	String getName();

	void setLiterals(List<IFilter> valuesLiterals);

	void removeSubLiteral(IFilter filter);

	void replaceSubLiteral(IFilter literal, IFilter mappedFilter);

}
