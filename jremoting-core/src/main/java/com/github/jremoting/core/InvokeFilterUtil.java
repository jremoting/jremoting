package com.github.jremoting.core;

import java.util.List;



public class InvokeFilterUtil {

	public static InvokeFilter link(List<InvokeFilter> filters) {
		
		if(filters == null || filters.size() < 1) {
			throw new IllegalArgumentException("filters must at least contain one InvokeFilter!");
		}
		
		for(int i= 0 ; i < filters.size(); i++) {
			InvokeFilter current = filters.get(i);
			InvokeFilter next = null;
			
			if(i + 1 < filters.size()) {
				next = filters.get(i+1);
				current.setNext(next);
			}
		}
		return filters.get(0);
	}
	
}
