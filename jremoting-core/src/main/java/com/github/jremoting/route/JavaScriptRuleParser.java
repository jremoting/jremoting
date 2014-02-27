package com.github.jremoting.route;

import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.util.JsonUtil;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;
import com.github.jremoting.util.StringUtil;

public class JavaScriptRuleParser implements RouteRuleParser {
	private final Logger logger = LoggerFactory.getLogger(JavaScriptRuleParser.class);
	
	public static void main(String[] args) throws Exception {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append("	defineRouteTables : function () {");
		builder.append("		return {\n");
		builder.append("			G1 : ['121222', '233232'],");
		builder.append("			G2 : ['ddddd']\n");
		builder.append("		}");
		builder.append("	},");
		builder.append("	defineServiceRule :function() {return 'G1';},");
		builder.append("	defineMethodRule :function(methodName,parameterTypeNames) {println(parameterTypeNames[0]);return 'G';},");
		builder.append("	defineParameterRule :function(methodName,parameterTypeNames,args) {println(args[0].group); return null;}");
		builder.append("}");

		RouteRule routeRule = parseRouteRule(builder.toString());

		Map<String, String[]> tables = routeRule.defineRouteTables();
		System.out.println(JsonUtil.toJson(tables));

		if (routeRule instanceof MethodRouteRule) {
			MethodRouteRule methodRouteRule = (MethodRouteRule) routeRule;
			String result = methodRouteRule.selectRouteTable("hello",
					new String[] { "int" });
			System.out.println("method rule return:" + result);
		}

		if (routeRule instanceof ParameterRouteRule) {
			ParameterRouteRule parameterRouteRule = (ParameterRouteRule) routeRule;
			String result = parameterRouteRule.selectRouteTable("hello",
					new String[] { "int" }, new Object[] { new ServiceConsumer(
							"interfaceName", "version", "group", null) });

			System.out.println("parameter rule return:" + result);
		}
	}

	@Override
	public RouteRule parse(String content) {
		try {
			return parseRouteRule(content);
		} catch (ScriptException e) {
			logger.error("invalid js route rule:" + e.getMessage(),e);
			return null;
		}
	}

	public interface JavaScriptRuleAdapter {
		
		Map<String, String[]> defineRouteTables();
		
		String serviceRule();

		String methodRule(String methodName, String[] parameterTypeNames);

		String parameterRule(String methodName,String[] parameterTypeNames, Object[] args);
		
		boolean containsServiceRule();

		boolean containsMethodRule();

		boolean containsParameterRule();
	}

	public static RouteRule parseRouteRule(String jsObject)
			throws ScriptException {

		if(StringUtil.isEmpty(jsObject)) {
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("var obj = ");
		builder.append(jsObject);
		builder.append(";\n");
		builder.append(JS_ADAPTER_DEFINE);

		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		engine.eval(builder.toString());

		Object adapter = engine.get("adapter");
		Invocable inv = (Invocable) engine;
		final JavaScriptRuleAdapter ruleAdapter = inv.getInterface(adapter,
				JavaScriptRuleAdapter.class);

		if(ruleAdapter.containsServiceRule() && ruleAdapter.containsMethodRule() && ruleAdapter.containsParameterRule()) {
			return new AllRouteRule() {

				@Override
				public String selectRouteTable(String methodName,
						String[] parameterTypeNames) {
					return ruleAdapter.methodRule(methodName,
							parameterTypeNames);
				}

				@Override
				public Map<String, String[]> defineRouteTables() {
					return ruleAdapter.defineRouteTables();
				}

				@Override
				public String selectRouteTable(String methodName,
						String[] parameterTypeNames, Object[] args) {
					return ruleAdapter.parameterRule(methodName,
							parameterTypeNames, args);
				}

				@Override
				public String selectRouteTable() {
					return ruleAdapter.serviceRule();
				}

			};
		}
		if (ruleAdapter.containsMethodRule() && ruleAdapter.containsParameterRule()) {
			return new MethodAndParameterRule() {
				
				@Override
				public String selectRouteTable(String methodName,
						String[] parameterTypeNames) {
					return ruleAdapter.methodRule(methodName,
							parameterTypeNames);
				}

				@Override
				public Map<String, String[]> defineRouteTables() {
					return ruleAdapter.defineRouteTables();
				}

				@Override
				public String selectRouteTable(String methodName,
						String[] parameterTypeNames, Object[] args) {
					return ruleAdapter.parameterRule(methodName,
							parameterTypeNames, args);
				}
			};
			
		} else if (ruleAdapter.containsMethodRule() && ruleAdapter.containsServiceRule()) {
			return new MethodAndServiceRule() {
				

				@Override
				public String selectRouteTable(String methodName,
						String[] parameterTypeNames) {
					return ruleAdapter.methodRule(methodName,
							parameterTypeNames);
				}

				@Override
				public Map<String, String[]> defineRouteTables() {
					return ruleAdapter.defineRouteTables();
				}

				@Override
				public String selectRouteTable() {
					return ruleAdapter.serviceRule();
				}
			};
		} else if (ruleAdapter.containsParameterRule() && ruleAdapter.containsServiceRule()) {
			return new ParameterAndServiceRule() {

				@Override
				public Map<String, String[]> defineRouteTables() {
					return ruleAdapter.defineRouteTables();
				}

				@Override
				public String selectRouteTable(String methodName,
						String[] parameterTypeNames, Object[] args) {
					return ruleAdapter.parameterRule(methodName,
							parameterTypeNames, args);
				}

				@Override
				public String selectRouteTable() {
					return ruleAdapter.serviceRule();
				}
			};
		} 
		else if(ruleAdapter.containsMethodRule()) {
			return new MethodRouteRule() {
				

				@Override
				public String selectRouteTable(String methodName,
						String[] parameterTypeNames) {
					return ruleAdapter.methodRule(methodName,
							parameterTypeNames);
				}

				@Override
				public Map<String, String[]> defineRouteTables() {
					return ruleAdapter.defineRouteTables();
				}
			};
		}
		else if(ruleAdapter.containsParameterRule()) {
			return new ParameterRouteRule() {
				
				@Override
				public Map<String, String[]> defineRouteTables() {
					return ruleAdapter.defineRouteTables();
				}

				@Override
				public String selectRouteTable(String methodName,
						String[] parameterTypeNames, Object[] args) {
					return ruleAdapter.parameterRule(methodName,
							parameterTypeNames, args);
				}
			};
		}
		else if(ruleAdapter.containsServiceRule()){
			return new ServiceRouteRule() {

				@Override
				public Map<String, String[]> defineRouteTables() {
					return ruleAdapter.defineRouteTables();
				}

				@Override
				public String selectRouteTable() {
					return ruleAdapter.serviceRule();
				}
			};
		}
		else {
			return null;
		}
	}

	private static interface AllRouteRule extends MethodRouteRule, ParameterRouteRule, ServiceRouteRule {
	}
	
	private static interface MethodAndParameterRule extends MethodRouteRule, ParameterRouteRule { 
	}
	private static interface MethodAndServiceRule extends MethodRouteRule, ServiceRouteRule { 
	}
	private static interface ParameterAndServiceRule extends ParameterRouteRule, ServiceRouteRule { 
	}

	

	private static final String JS_ADAPTER_DEFINE;
	static {
		StringBuilder builder = new StringBuilder();
		builder.append("var adapter = {																					 ");
		builder.append("	defineRouteTables:function() {																 ");
		builder.append("		var map = new java.util.HashMap();														 ");
		builder.append("		var tables = obj.defineRouteTables();													 ");
		builder.append("		for(var tableName in tables) {															 ");
		builder.append("			var ipPatterns = tables[tableName];													 ");
		builder.append("			var array = java.lang.reflect.Array.newInstance(java.lang.String, ipPatterns.length);");
		builder.append("			for(var i = 0 ; i < ipPatterns.length; i++) {                                        ");
		builder.append("				array[i] = ipPatterns[i];                                                        ");
		builder.append("			}                                                                                    ");
		builder.append("			map.put(tableName,array);                                                            ");
		builder.append("		}                                                                                        ");
		builder.append("		return map;                                                                              ");
		builder.append("	},                                                                                           ");
		builder.append("	serviceRule:function(methodName,parameterTypeNames) {                                   ");
		builder.append("		return obj.defineServiceRule(methodName,parameterTypeNames);                              ");
		builder.append("	},                                                                                           ");
		builder.append("	methodRule:function(methodName,parameterTypeNames) {                                   ");
		builder.append("		return obj.defineMethodRule(methodName,parameterTypeNames);                              ");
		builder.append("	},                                                                                           ");
		builder.append("	parameterRule:function(methodName,parameterTypeNames,args) {                           ");
		builder.append("		return  obj.defineParameterRule(methodName,parameterTypeNames,args);                     ");
		builder.append("	},                                                                                           ");
		builder.append("	containsServiceRule:function() {                                                              ");
		builder.append("		return obj.defineServiceRule != undefined;                                                ");
		builder.append("	},                                                                                           ");
		builder.append("	containsMethodRule:function() {                                                              ");
		builder.append("		return obj.defineMethodRule != undefined;                                                ");
		builder.append("	},                                                                                           ");
		builder.append("	containsParameterRule:function() {                                                           ");
		builder.append("		return obj.defineParameterRule != undefined;                                             ");
		builder.append("	}                                                                                            ");
		builder.append("}                                                                                                ");

		JS_ADAPTER_DEFINE = builder.toString();
	}

}
