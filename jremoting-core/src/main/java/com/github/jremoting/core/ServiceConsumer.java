package com.github.jremoting.core;

import java.util.concurrent.CountDownLatch;

import com.github.jremoting.util.concurrent.ListenableFuture;

public class ServiceConsumer extends ServiceParticipant {
	
	private Serializer serializer;
	private String asyncInterfaceName;
	
	private final CountDownLatch initLatch = new CountDownLatch(1);
	
	private boolean init = false;
	private final RpcClient rpcClient;
	
	
	public ServiceConsumer(String interfaceName, String version, RpcClient rpcClient) {
		super(interfaceName, version, null);
		this.rpcClient = rpcClient;
	}
	
	public ServiceConsumer(String interfaceName, String version, String group, RpcClient rpcClient) {
		super(interfaceName, version, group);
		this.rpcClient = rpcClient;
	}
	
	public void start() {
		this.rpcClient.register(this);
	}
	
	public Object invoke(String methodName, 
			String[] parameterTypeNames, Object[] args) {
		Invoke invoke = new Invoke(this.getInterfaceName(), this.getVersion(), methodName, this.getSerializer(),
				args, parameterTypeNames);
		invoke.setTimeout(this.getTimeout());
		invoke.setRemoteAddress(this.getAddress());
		return rpcClient.invoke(invoke);
	}

	
	public ListenableFuture<?> $invoke(String methodName, String[] parameterTypeNames, Object[] args) {
		Invoke invoke = new Invoke(this.getInterfaceName(), this.getVersion(), methodName, this.getSerializer(),
				args, parameterTypeNames);
		invoke.setTimeout(this.getTimeout());
		invoke.setRemoteAddress(this.getAddress());
		invoke.setAsync(true);
		return (ListenableFuture<?>)rpcClient.invoke(invoke);
	}

	public void waitFirstSubscribeFinishIfNeeded() throws InterruptedException  {
		if(!init) {
			initLatch.await();
		}
	}
	
	public void setFirstSubscribeFinished() {
		init = true;
		initLatch.countDown();
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

	public String getAsyncInterfaceName() {
		return asyncInterfaceName;
	}

	public void setAsyncInterfaceName(String asyncInterfaceName) {
		this.asyncInterfaceName = asyncInterfaceName;
	}
}
