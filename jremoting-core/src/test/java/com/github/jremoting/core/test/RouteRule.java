package com.github.jremoting.core.test;

import java.util.HashMap;
import java.util.Map;

import com.github.jremoting.route.MethodRouteRule;
import com.github.jremoting.route.ParameterRouteRule;
import com.github.jremoting.route.ServiceRouteRule;

public class RouteRule implements MethodRouteRule, ServiceRouteRule, ParameterRouteRule  {

	@Override
	public String selectRouteTable(String methodName,
			String[] parameterTypeNames) {
		
		if("hello".equals(methodName)){
			return "G1";
		}
		
		return null;
	}

	@Override
	public Map<String, String[]> createRouteTables() {
		Map<String, String[]> tables = new HashMap<String, String[]>();
		tables.put("G1", new String[]{"121212","121212"});
		return tables;
	}

	@Override
	public String selectRouteTable(String methodName,
			String[] parameterTypeNames, Object[] args) {
		return null;
	}

}
