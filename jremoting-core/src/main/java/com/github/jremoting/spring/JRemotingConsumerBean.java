package com.github.jremoting.spring;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;

import com.github.jremoting.core.GenericService;
import com.github.jremoting.core.RpcClient;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.invoke.ClientInvocationHandler;


@SuppressWarnings("rawtypes")
public class JRemotingConsumerBean extends GenericService implements FactoryBean {

	public JRemotingConsumerBean(String interfaceName, String version,
			RpcClient rpcClient) {
		super(interfaceName, version, rpcClient);
	}

	@Override
	public Object getObject() throws Exception {
		return Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class<?>[]{getObjectType()}, 
				new ClientInvocationHandler(getRpcClient(), getSerializer(), getInterfaceName(), getVersion(), getAddress(), getTimeout()));
	}
	
	@Override
	public Class<?> getObjectType() {
		try {
			return this.getClass().getClassLoader().loadClass(this.getInterfaceName());
		} catch (ClassNotFoundException e) {
			throw new RemotingException(e);
		}
	}
	@Override
	public boolean isSingleton() {
		return true;
	}
}
