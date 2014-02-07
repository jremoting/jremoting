package com.github.jremoting.spring;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;

import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.Serializer;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.invoker.ClientInvocationHandler;
import com.github.jremoting.invoker.ClientRpcInvoker;

@SuppressWarnings("rawtypes")
public class JRemotingConsumerBean implements FactoryBean {

	private String interfaceName;
	private String version;
	private Protocal protocal;
	private Serializer serializer;
	private ClientRpcInvoker clientRpcInvoker;
	public ClientRpcInvoker getClientRpcInvoker() {
		return clientRpcInvoker;
	}

	public void setClientRpcInvoker(ClientRpcInvoker clientRpcInvoker) {
		this.clientRpcInvoker = clientRpcInvoker;
	}
	private String address;
	private int invokeTimeout;
	 
	
	@Override
	public Object getObject() throws Exception {
		return Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class<?>[]{getObjectType()}, 
				new ClientInvocationHandler(clientRpcInvoker, protocal, serializer, interfaceName, version, address));
	}
	
	@Override
	public Class<?> getObjectType() {
		try {
			return this.getClass().getClassLoader().loadClass(this.interfaceName);
		} catch (ClassNotFoundException e) {
			throw new RemotingException(e);
		}
	}
	@Override
	public boolean isSingleton() {
		return true;
	}
	
	public String getInterfaceName() {
		return interfaceName;
	}
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Protocal getProtocal() {
		return protocal;
	}
	public void setProtocal(Protocal protocal) {
		this.protocal = protocal;
	}
	public Serializer getSerializer() {
		return serializer;
	}
	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}
	public int getInvokeTimeout() {
		return invokeTimeout;
	}
	public void setInvokeTimeout(int invokeTimeout) {
		this.invokeTimeout = invokeTimeout;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
}
