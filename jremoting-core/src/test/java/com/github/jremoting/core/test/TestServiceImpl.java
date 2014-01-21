package com.github.jremoting.core.test;

public class TestServiceImpl implements TestService {

	@Override
	public String hello(String name) {
		return "hello,"+ name;
	}

}
