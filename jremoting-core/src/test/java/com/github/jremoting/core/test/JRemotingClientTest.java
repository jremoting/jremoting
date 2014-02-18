package com.github.jremoting.core.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.github.jremoting.core.GenericService;
import com.github.jremoting.core.RpcClient;
import com.github.jremoting.core.test.TestService.HelloInput;
import com.github.jremoting.core.test.TestService.HelloOutput;
import com.github.jremoting.util.concurrent.FutureListener;
import com.github.jremoting.util.concurrent.ListenableFuture;


public class JRemotingClientTest {
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("client-test.xml");
		
		RpcClient rpcClient = context.getBean(RpcClient.class);
		
		GenericService genericService =  new GenericService("com.github.jremoting.core.test.TestService", "1.0", rpcClient).start();
		
		HashMap<String, Object> genericInput = new HashMap<String, Object>();
		genericInput.put("id", 1221);
		genericInput.put("msg", "generic call!");
		
		//同步调用泛型调用
		Object result = genericService.invoke("hello",
				new String[]{"com.github.jremoting.core.test.TestService$HelloInput", "int"}, 
				new Object[]{genericInput, 2112});
	
		System.out.println(JSON.toJSONString(result));

		
		//同步调用
		TestService testService = context.getBean(TestService.class);

		testService.hello1();
		
		
		
		//异步future调用
		AsyncTestService asyncTestService = (AsyncTestService)testService;
		HelloInput input = new HelloInput();
		input.setId(12);
		input.setMsg("async test");
		Future<HelloOutput> future =  asyncTestService.$hello(new HelloInput(), 1212);
		System.out.println("async:" + JSON.toJSONString(future.get()));
		
		//异步callback调用
		final AtomicReference<Future<HelloOutput>> holder = new AtomicReference<Future<HelloOutput>>();
		Future<HelloOutput> f =  asyncTestService.$hello(input, 1212, new Runnable() {
			
			@Override
			public void run() {
				try {
					System.out.println("async:callback"+ JSON.toJSONString(holder.get().get()));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		holder.set(f);
		
		
		
		//异步ListenableFuture调用，更优雅但是对jremoting有直接依赖
		final ListenableFuture<HelloOutput> furFuture =  asyncTestService.$hello(input, 1212);
		furFuture.addListener(new FutureListener<TestService.HelloOutput>() {
			
			@Override
			public void operationComplete(ListenableFuture<HelloOutput> future) {
				if(future.isSuccess()) {
					System.out.println(future.result().getMsg());
				}
			}
		});
		
		
		
		
		
		System.in.read();
		
		context.close();
	}
}
