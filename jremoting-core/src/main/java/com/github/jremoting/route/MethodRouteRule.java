package com.github.jremoting.route;

public interface MethodRouteRule extends RouteRule {
	public String defineMethodRule(String methodName, String[] parameterTypeNames);
}
