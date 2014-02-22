package com.github.jremoting.spring;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;

import com.github.jremoting.core.RpcClient;
import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.invoke.ClientInvocationHandler;
import com.github.jremoting.util.ReflectionUtil;


@SuppressWarnings("rawtypes")
public class JRemotingConsumerBean  implements FactoryBean {
	
	private ServiceConsumer consumer;
	private String asyncInterfaceName;
	
	public JRemotingConsumerBean(String interfaceName, String version,
			RpcClient rpcClient) {
		this.consumer = new ServiceConsumer(interfaceName, version, rpcClient);
	}

	@Override
	public Object getObject() throws Exception {
		if(this.asyncInterfaceName == null) {
			return Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class<?>[]{
					ReflectionUtil.findClass(this.consumer.getInterfaceName())}, 
					new ClientInvocationHandler(this.consumer));
		}
		else {
			return Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class<?>[]{
				ReflectionUtil.findClass(this.consumer.getInterfaceName()), 
				ReflectionUtil.findClass(asyncInterfaceName)}, 
				new ClientInvocationHandler(this.consumer));
		}
	}
	
	@Override
	public Class<?> getObjectType() {
		try {
			return this.getClass().getClassLoader().loadClass(this.consumer.getInterfaceName());
		} catch (ClassNotFoundException e) {
			throw new RemotingException(e);
		}
	}
	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setAsyncInterfaceName(String asyncInterfaceName) {
		this.asyncInterfaceName = asyncInterfaceName;
	}
	
	public void setRemoteAddress(String remoteAddress) {
		this.consumer.setRemoteAddress(remoteAddress);
	}
}
