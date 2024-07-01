package com.garganttua.api.spec.filter;

import java.util.List;

public interface IGGAPIFilter extends Cloneable {

	Object getValue();
	
	void setValue(Object value);

	IGGAPIFilter clone();

	List<IGGAPIFilter> getLiterals();

	String getName();

	void setLiterals(List<IGGAPIFilter> valuesLiterals);

	void removeSubLiteral(IGGAPIFilter filter);

	void replaceSubLiteral(IGGAPIFilter literal, IGGAPIFilter mappedFilter);

}
