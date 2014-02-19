package com.github.jremoting.core.test;

import java.util.Date;
import java.util.concurrent.Executor;
import com.github.jremoting.util.concurrent.AsyncCallback;
import com.github.jremoting.util.concurrent.Executors;

public class TestServiceImpl implements TestService {

	@Override
	public HelloOutput hello(HelloInput input, int id) {
		HelloOutput output = new HelloOutput();
		output.setId(input.getId() + id);
		output.setMsg("hello," + input.getMsg());
		return output;
	}
	
	static Executor executor = Executors.newExecutor(1, 1, 44);
	
	public void $hello(HelloInput input, int id, final AsyncCallback<HelloOutput> asyncCallback) {
		
		executor.execute(new Runnable() {
			@Override
			public void run() {
				HelloOutput output =  new HelloOutput();
				output.setMsg("server async result");
				asyncCallback.setResult(output);
			}
		});
	
	}

	@Override
	public void hello1() {
		System.out.println("ok!");
	}

	@Override
	public void hello3(char c, boolean bb,int i, long l, double d, float f, short sb, byte b,
			String s, Date date, java.sql.Date dd) {
		System.out.println(c);
		System.out.println(bb);
		System.out.println(i);
		System.out.println(l);
		System.out.println(d);
		System.out.println(f);
		System.out.println(sb);
		System.out.println(b);
		System.out.println(s);
		System.out.println(date);
		System.out.println(dd);
	}

}
