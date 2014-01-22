package com.github.jremoting.core.test;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.github.jremoting.core.test.TestService.HelloInput;
import com.github.jremoting.core.test.TestService.HelloOutput;

public class ClientTest {
	public static void main(String[] args) throws IOException, SecurityException, NoSuchMethodException {
		
		/*TestServiceImpl testServiceImpl = new TestServiceImpl();
		testServiceImpl.getClass().getMethod("hello", new Class<?>[]{HelloInput.class,Integer.class});
		*/
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("client-test.xml");
		
		TestService testService = context.getBean(TestService.class);
		
		for (int i = 0; i <10; i++) {
			HelloInput input = new HelloInput();
			input.setId(i);
			HelloOutput result = testService.hello(input,i);
			System.out.println(JSON.toJSON(result));
		}
		
		for (int i = 0; i < 10; i++) {
			testService.hello1();
		}
		
		for (int i = 0; i <10; i++) {
			HelloInput input = new HelloInput();
			input.setId(i);
			HelloOutput result = testService.hello(input,i);
			System.out.println(JSON.toJSON(result));
		}
		
		System.in.read();
		
		context.close();
		
	}
}
