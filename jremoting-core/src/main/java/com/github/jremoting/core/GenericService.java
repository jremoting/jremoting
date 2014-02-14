package com.github.jremoting.core;

import com.github.jremoting.core.ServiceParticipantInfo.ParticipantType;
import com.github.jremoting.util.NetUtil;

public class GenericService  {
	 
	private final String interfaceName;
	private final String version;
	private long timeout;
	private final RpcClient rpcClient;
	private Serializer serializer;
	private String address;

	public GenericService(String interfaceName, String version, RpcClient rpcClient) {
		this.interfaceName = interfaceName;
		this.version = version;
		this.rpcClient = rpcClient;
	}
	public Object invoke(String methodName, 
			String[] parameterTypeNames, Object[] args) {
		Invoke invoke = new Invoke(interfaceName, version, methodName, serializer, args, parameterTypeNames);
		invoke.setTimeout(timeout);
		return rpcClient.invoke(invoke);
	}
	
	public void start() {
		this.rpcClient.register(new ServiceParticipantInfo(this.interfaceName + ":" + this.version , NetUtil.getLocalHost(), ParticipantType.CONSUMER));
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	public String getVersion() {
		return version;
	}


	public RpcClient getRpcClient() {
		return rpcClient;
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

	public String getInterfaceName() {
		return interfaceName;
	}

}
