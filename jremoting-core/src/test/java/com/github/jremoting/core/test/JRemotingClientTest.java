package com.github.jremoting.core.test;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.github.jremoting.core.GenericService;
import com.github.jremoting.core.RpcClient;


public class JRemotingClientTest {
	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("client-test.xml");
		
		RpcClient rpcClient = context.getBean(RpcClient.class);
		
		GenericService genericService =  new GenericService("com.github.jremoting.core.test.TestService", "1.0", rpcClient);
	
		genericService.start();
		
		HashMap<String, Object> genericInput = new HashMap<String, Object>();
		genericInput.put("id", 1221);
		genericInput.put("msg", "generic call!");
		
		Object result = genericService.invoke("hello",
				new String[]{"com.github.jremoting.core.test.TestService$HelloInput", "int"}, 
				new Object[]{genericInput, 2112});
	
		System.out.println(JSON.toJSONString(result));
		
		
		System.in.read();
		
		context.close();
	}
}
