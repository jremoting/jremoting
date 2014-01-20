package com.github.jremoting.spring;

import com.github.jremoting.core.InvokePipeline;
import com.github.jremoting.core.ServerDispatcher;

public class JRemotingProviderBean  {

	private String serviceName;
	private String serviceVersion;
	private Object target;
	
	private ServerDispatcher dispatcher;
	private InvokePipeline pipeline;
	
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
	public Object getTarget() {
		return target;
	}
	public void setTarget(Object target) {
		this.target = target;
	}

	public InvokePipeline getPipeline() {
		return pipeline;
	}

	public void setPipeline(InvokePipeline pipeline) {
		this.pipeline = pipeline;
	}

	public ServerDispatcher getDispatcher() {
		return dispatcher;
	}
	
	public void setDispatcher(ServerDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
}
