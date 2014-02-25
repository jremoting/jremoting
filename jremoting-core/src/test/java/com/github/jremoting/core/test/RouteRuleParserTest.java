package com.github.jremoting.core.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.github.jremoting.route.JavaRouteRuleParser;
import com.github.jremoting.route.RouteRule;

public class RouteRuleParserTest {
	public static void main(String[] args) throws IOException {

		String source = readFile("D:\\tmp\\route.rule");
		JavaRouteRuleParser parser = new JavaRouteRuleParser();
		RouteRule rule = parser.complieRouteRule(source);

		System.out.println(JSON.toJSONString(rule.createRouteTables()));
	}

	static String readFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}
}
