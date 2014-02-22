package com.github.jremoting.spring;

import com.github.jremoting.core.RpcServer;
import com.github.jremoting.core.ServiceProvider;

public class JRemotingProviderBean   {

	private ServiceProvider provider;
	
	public JRemotingProviderBean(String interfaceName,String version, Object target, RpcServer rpcServer ) {
		this.provider = new ServiceProvider(interfaceName, version, rpcServer, target);
	}
	
	public void start() {
		this.provider.start();
	}
}
