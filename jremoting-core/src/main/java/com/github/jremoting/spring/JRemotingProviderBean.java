package com.github.jremoting.spring;

import com.github.jremoting.core.RpcServer;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.invoker.ServerRpcInvoker;

public class JRemotingProviderBean implements ServiceProvider    {

	private String serviceName;
	private String serviceVersion;
	private Object target;
	private ServerRpcInvoker serverRpcInvoker;
	private RpcServer rpcServer;
	
	public String getServiceName() {
		return serviceName;
	}
	
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServiceVersion() {
		return serviceVersion;
	}
	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}
	
	@Override
	public String getServiceId() {
		return this.getServiceName() + ":" + this.getServiceVersion();
	}

	@Override
	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	public ServerRpcInvoker getServerRpcInvoker() {
		return serverRpcInvoker;
	}

	public void setServerRpcInvoker(ServerRpcInvoker serverRpcInvoker) {
		this.serverRpcInvoker = serverRpcInvoker;
		this.serverRpcInvoker.register(this);
	}

	public RpcServer getRpcServer() {
		return rpcServer;
	}

	public void setRpcServer(RpcServer rpcServer) {
		this.rpcServer = rpcServer;
		this.rpcServer.start();
	}
	
}
