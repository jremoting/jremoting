package com.github.jremoting.core.test;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClientTest {
	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("client-test.xml");
		
		TestService testService = context.getBean(TestService.class);
		
		for (int i = 0; i <100; i++) {
			String result = testService.hello("xhan" + i);
			System.out.println(result);
		}
		
		System.in.read();
		
		context.close();
		
	}
}
