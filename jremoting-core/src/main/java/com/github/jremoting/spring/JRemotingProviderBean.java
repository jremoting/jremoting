package com.github.jremoting.spring;

import java.util.concurrent.Executor;

import com.github.jremoting.core.RpcServer;
import com.github.jremoting.core.ServiceProvider;

public class JRemotingProviderBean implements ServiceProvider    {

	private final String interfaceName;
	private final String version;
	private final Object target;
	private final RpcServer rpcServer;
	private boolean supportAsync = false;
	private Executor executor;
	
	public JRemotingProviderBean(String interfaceName,String version, Object target, RpcServer rpcServer ) {
		this.interfaceName = interfaceName;
		this.version = version;
		this.target = target;
		this.rpcServer = rpcServer;
	}
	
	public void start() {
		this.rpcServer.register(this);
	}
	
	public String getInterfaceName() {
		return interfaceName;
	}
	
	public String getVersion() {
		return version;
	}

	@Override
	public String getServiceName() {
		return this.interfaceName + ":" + this.version;
	}

	@Override
	public Object getTarget() {
		return target;
	}

	public RpcServer getRpcServer() {
		return rpcServer;
	}

	@Override
	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}
	
	@Override
	public boolean isSupportAsync() {
		return supportAsync;
	}

	public void setSupportAsync(boolean supportAsync) {
		this.supportAsync = supportAsync;
	}
}
