package com.github.jremoting.route;

import java.util.concurrent.atomic.AtomicInteger;

import com.github.jremoting.util.StringUtil;


public class JavaRouteRuleParser implements RouteRuleParser {

	private AtomicInteger id = new AtomicInteger(0);

	@Override
	public RouteRule parse(String content) {
	
		return complieRouteRule(content);
		
	}

	public RouteRule complieRouteRule(String classSource) {
	
		if (StringUtil.isEmpty(classSource)) {
			return null;
		}

		String packageName = "com.github.jremoting.routerule"
				+ id.incrementAndGet();
		String className = packageName + ".RouteRule";

		StringBuilder builder = new StringBuilder(classSource.length()
				+ packageName.length() + 2);
		builder.append(packageName).append(";\n").append(classSource);

		try {
			Object obj = JavaRuntimeCompiler.compileForInstance(className,
					builder);
			if (obj instanceof RouteRule) {
				return (RouteRule) obj;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
}
