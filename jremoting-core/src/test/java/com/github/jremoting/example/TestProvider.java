package com.github.jremoting.example;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestProvider {
	public static void main(String[] args) throws IOException {
		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("server-context.xml");
		
		
		System.in.read();
		
		context.close();
	}
}
