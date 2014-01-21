package com.github.jremoting.core.test;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerTest {
	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("server-test.xml");
		System.in.read();
		context.close();
	}
}
