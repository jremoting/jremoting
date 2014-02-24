package com.github.jremoting.spring;

import com.github.jremoting.core.RpcServer;
import com.github.jremoting.core.ServiceProvider;

public class JRemotingProviderBean extends ServiceProvider   {

	public JRemotingProviderBean(String interfaceName, String version,
			RpcServer rpcServer, Object target) {
		super(interfaceName, version, rpcServer, target);
	}

}
