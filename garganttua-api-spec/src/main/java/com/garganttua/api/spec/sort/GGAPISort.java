package com.garganttua.api.spec.sort;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter	
public class GGAPISort implements IGGAPISort {
	
	private String fieldName;
	
	private GGAPISortDirection direction;
}
