package com.github.jremoting.route;

import java.util.Map;

public interface RouteRule {
	Map<String, String[]> defineRouteTables();
	String selectRouteTable(String methodName, String[] parameterTypeNames, Object[] args);
}
