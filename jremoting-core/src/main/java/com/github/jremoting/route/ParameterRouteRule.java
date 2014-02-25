package com.github.jremoting.route;

public interface ParameterRouteRule extends RouteRule  {
	String selectRouteTable(String methodName, String[] parameterTypeNames, Object[] args);
}
