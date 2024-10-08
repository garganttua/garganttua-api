package com.garganttua.api.spec.sort;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter	
@NoArgsConstructor
public class GGAPISort implements IGGAPISort {
	
	private String fieldName;
	
	private GGAPISortDirection direction;
}
