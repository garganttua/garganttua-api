package com.garganttua.api.commons.sort;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter	
@NoArgsConstructor
public class Sort implements ISort {
	
	private String fieldName;
	
	private SortDirection direction;
}
