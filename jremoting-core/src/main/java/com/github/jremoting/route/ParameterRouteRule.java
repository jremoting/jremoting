package com.github.jremoting.route;

public interface ParameterRouteRule extends RouteRule  {
	String defineParameterRule(String methodName, String[] parameterTypeNames, Object[] args);
}
