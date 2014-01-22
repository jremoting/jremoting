package com.github.jremoting.core.test;

public class TestServiceImpl implements TestService {

	@Override
	public HelloOutput hello(HelloInput input, Integer id) {
		HelloOutput output = new HelloOutput();
		output.setId(input.getId() + id);
		output.setMsg("hello," + input.getMsg());
		return output;
	}

	@Override
	public void hello1() {
		System.out.println("ok!");
	}


}
