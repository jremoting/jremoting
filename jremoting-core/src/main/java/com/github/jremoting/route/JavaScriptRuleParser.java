package com.github.jremoting.route;

import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
		builder.append("	selectRouteTable :function(methodName,parameterTypeNames,args) {println(args[0].group); return null;}");
		builder.append("}");

		RouteRule routeRule = parseRouteRule(builder.toString());

		Map<String, String[]> tables = routeRule.defineRouteTables();
		System.out.println(JsonUtil.toJson(tables));

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
	    RouteRule routeRule = inv.getInterface(adapter,RouteRule.class);
		
		return routeRule;

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
		builder.append("	selectRouteTable:function(methodName,parameterTypeNames,args) {                           ");
		builder.append("		return  obj.selectRouteTable(methodName,parameterTypeNames,args);                     ");
		builder.append("	}                                                                                           ");
		builder.append("}                                                                                                ");

		JS_ADAPTER_DEFINE = builder.toString();
	}

}
