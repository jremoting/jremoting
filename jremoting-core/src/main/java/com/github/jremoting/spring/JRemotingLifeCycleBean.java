package com.github.jremoting.spring;

import java.util.List;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.github.jremoting.core.RpcClient;
import com.github.jremoting.core.RpcServer;

public class JRemotingLifeCycleBean implements ApplicationListener<ApplicationEvent>  {
	
	private List<RpcClient> rpcClients;
	private List<RpcServer> rpcServers;

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if(event instanceof ContextRefreshedEvent) {
			for (RpcClient rpcClient : rpcClients) {
				rpcClient.start();
			}
			for(RpcServer rpcServer : rpcServers) {
				rpcServer.start();
			}
			
		}
		else if (event instanceof ContextClosedEvent) {
			for(RpcServer rpcServer : rpcServers) {
				rpcServer.close();
			}
			for (RpcClient rpcClient : rpcClients) {
				rpcClient.close();
			}
		}
	}
	public List<RpcClient> getRpcClients() {
		return rpcClients;
	}

	public void setRpcClients(List<RpcClient> rpcClients) {
		this.rpcClients = rpcClients;
	}

	public List<RpcServer> getRpcServers() {
		return rpcServers;
	}

	public void setRpcServers(List<RpcServer> rpcServers) {
		this.rpcServers = rpcServers;
	}

}
