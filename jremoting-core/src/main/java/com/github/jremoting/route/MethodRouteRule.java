package com.github.jremoting.route;

public interface MethodRouteRule extends RouteRule {
	public String selectRouteTable(String methodName, String[] parameterTypeNames);
}
