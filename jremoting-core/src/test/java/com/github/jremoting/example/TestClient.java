package com.github.jremoting.example;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.github.jremoting.core.RpcClient;
import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.util.concurrent.FutureListener;
import com.github.jremoting.util.concurrent.ListenableFuture;

public class TestClient {
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		
		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("client-context.xml");
		
		HelloService helloService = context.getBean(HelloService.class);
		
		//同步调用
		String result = helloService.hello("jremoting");
		
		System.out.println(result);
		
		//异步调用
		AsyncHelloService asyncHelloService = (AsyncHelloService)helloService;
		
		ListenableFuture<String> future = asyncHelloService.$hello("jremoting async invoke!");
		
		System.out.println(future.get());
		
		
		//异步listener方式调用，注意operationComplete是在jremoting-context.xml中配置的专门executor上执行的。也可以自己指定executor
		future = asyncHelloService.$hello("jremoting async use future listener!");
		
		future.addListener(new FutureListener<String>() {
			
			@Override
			public void operationComplete(ListenableFuture<String> future) {
				if(future.isSuccess()) {
					System.out.println(future.result());
				}
			}
		});
		
		
		//如果consumer端，不想依赖provider定义的接口，也可以直接调用远程方法，不过要把复杂对象都用map来代替，返回结果也一样
		RpcClient rpcClient = context.getBean(RpcClient.class);
		ServiceConsumer consumer = new ServiceConsumer("com.github.jremoting.example.HelloService", "1.0", rpcClient).start();
		consumer.setAddress("10.10.53.160:8686");
		Object obj = consumer.invoke("hello", new String[]{java.lang.String.class.getName()}, new Object[]{"generic invoke!"});
		
		System.out.println(obj);
		
		
		//关闭容器退出
		System.in.read();
		context.close();
	}
}
