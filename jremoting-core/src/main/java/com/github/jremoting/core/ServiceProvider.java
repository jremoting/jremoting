package com.github.jremoting.core;

import java.util.concurrent.ExecutorService;

public class  ServiceProvider extends ServiceParticipant   {

	
	public ServiceProvider(String interfaceName, String version, String group) {
		super(interfaceName, version, group);
	}
	public ServiceProvider(String interfaceName, String version,RpcServer rpcServer, Object target) {
		super(interfaceName, version, null);
		this.rpcServer = rpcServer;
		this.target = target;
	}
	public ServiceProvider(String interfaceName, String version, String group, RpcServer rpcServer, Object target) {
		super(interfaceName, version, group);
		this.rpcServer = rpcServer;
		this.target = target;
	}
	
	private Object target;
	private boolean supportAsync= false;
	private ExecutorService executor;
	private RpcServer rpcServer;
	private String ip;
	
	@Override
	public void setAddress(String address) {
		super.setAddress(address);
		this.setIp(address.split(":")[0]);
	}
	
	public void start() {
		this.rpcServer.register(this);
	}
	
	public Object getTarget() {
		return target;
	}
	public boolean isSupportAsync() {
		return supportAsync;
	}
	public void setSupportAsync(boolean supportAsync) {
		this.supportAsync = supportAsync;
	}
	public ExecutorService getExecutor() {
		return executor;
	}
	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	
}
