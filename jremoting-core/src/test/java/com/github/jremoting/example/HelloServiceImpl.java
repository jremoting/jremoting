package com.github.jremoting.example;

public class HelloServiceImpl implements HelloService {

	@Override
	public String hello(String name) {
		return "hello,"+ name;
	}

}
