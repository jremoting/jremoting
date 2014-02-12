package com.github.jremoting.core.test;

import java.io.IOException;
import java.util.Date;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.github.jremoting.core.test.TestService.HelloInput;
import com.github.jremoting.core.test.TestService.HelloOutput;

public class JRemotingClientTest {
	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("client-test.xml");
		
		TestService testService = context.getBean(TestService.class);
		
		
		for (int i = 0; i < 100; i++) {
			
			testService.hello3('0', false, 1, 2L, 3D, 4f, (short) 5, (byte) 6, "7",
					new Date(), new java.sql.Date(12121212));

			HelloInput input = new HelloInput();
			input.setId(1212);

			HelloOutput result = testService.hello(input, 324);

			System.out.println(JSON.toJSON(result));

			testService.hello1();
		  
		}
	
	
		
		
		System.in.read();
		
		context.close();
	}
}
