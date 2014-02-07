package com.github.jremoting.spring;

import com.github.jremoting.core.RpcServer;
import com.github.jremoting.core.ServiceProvider;

public class JRemotingProviderBean implements ServiceProvider    {

	private String interfaceName;
	private String version;
	private Object target;
	private RpcServer rpcServer;
	
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
	
	@Override
	public String getServiceName() {
		return this.interfaceName + ":" + this.version;
	}

	@Override
	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	public RpcServer getRpcServer() {
		return rpcServer;
	}

	public void setRpcServer(RpcServer rpcServer) {
		this.rpcServer = rpcServer;
		this.rpcServer.register(this);
		this.rpcServer.start();
	}
	
}
