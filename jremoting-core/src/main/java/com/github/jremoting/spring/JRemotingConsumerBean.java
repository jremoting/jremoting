package com.github.jremoting.spring;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;

import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.RpcClient;
import com.github.jremoting.core.Serializer;
import com.github.jremoting.core.ServiceParticipantInfo;
import com.github.jremoting.core.ServiceParticipantInfo.ParticipantType;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.invoke.ClientInvocationHandler;
import com.github.jremoting.util.NetUtil;

@SuppressWarnings("rawtypes")
public class JRemotingConsumerBean implements FactoryBean {

	private String interfaceName;
	private String version;
	private Protocal protocal;
	private Serializer serializer;
	private RpcClient rpcClient;

	private String address;
	private long timeout;
	 
	
	@Override
	public Object getObject() throws Exception {
		return Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class<?>[]{getObjectType()}, 
				new ClientInvocationHandler(rpcClient, protocal, serializer, interfaceName, version, address, timeout));
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
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

	public RpcClient getRpcClient() {
		return rpcClient;
	}

	public void setRpcClient(RpcClient rpcClient) {
		this.rpcClient = rpcClient;
		this.rpcClient.register(new ServiceParticipantInfo(this.interfaceName + ":" + this.version
				, NetUtil.getLocalHost(), ParticipantType.CONSUMER));
		
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
