package com.github.jremoting.core;

import com.github.jremoting.util.concurrent.ListenableFuture;

public class ServiceConsumer extends ServiceParticipant {
	
	private Serializer serializer;
	
	private final RpcClient rpcClient;
	
	private String remoteAddress;
	
	public ServiceConsumer(String interfaceName, String version, RpcClient rpcClient) {
		super(interfaceName, version, null);
		this.rpcClient = rpcClient;
	}
	
	public ServiceConsumer(String interfaceName, String version, String group, RpcClient rpcClient) {
		super(interfaceName, version, group);
		this.rpcClient = rpcClient;
	}
	
	public ServiceConsumer start() {
		this.rpcClient.register(this);
		return this;
	}
	
	public Object invoke(String methodName, 
			String[] parameterTypeNames, Object[] args) {
		Invoke invoke = new Invoke(this.getInterfaceName(), this.getVersion(),this.getGroup(), methodName, this.getSerializer(),
				args, parameterTypeNames);
		invoke.setTimeout(this.getTimeout());
		invoke.setConsumer(this);
		return rpcClient.invoke(invoke);
	}

	
	public ListenableFuture<?> $invoke(String methodName, String[] parameterTypeNames, Object[] args) {
		Invoke invoke = new Invoke(this.getInterfaceName(), this.getVersion(),this.getGroup(), methodName, this.getSerializer(),
				args, parameterTypeNames);
		invoke.setTimeout(this.getTimeout());
		invoke.setAsync(true);
		invoke.setConsumer(this);
		return (ListenableFuture<?>)rpcClient.invoke(invoke);
	}

	public Serializer getSerializer() {
		return serializer;
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

	public RpcClient getRpcClient() {
		return rpcClient;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
}
