package com.github.jremoting.spring;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;

import com.github.jremoting.core.ClientInvoker;
import com.github.jremoting.core.InvokePipeline;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.Serializer;
import com.github.jremoting.exception.RpcException;

@SuppressWarnings("rawtypes")
public class JRemotingConsumerBean implements FactoryBean {

	private String serviceName;
	private String serviceVersion;
	private Protocal protocal;
	private Serializer serializer;
	private InvokePipeline pipeline;
	
	private int invokeTimeout;
	 
	
	@Override
	public Object getObject() throws Exception {
		return Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class<?>[]{getObjectType()}, 
				new ClientInvoker(pipeline, protocal, serializer, serviceName, serviceVersion));
	}
	
	@Override
	public Class<?> getObjectType() {
		try {
			return this.getClass().getClassLoader().loadClass(this.serviceName);
		} catch (ClassNotFoundException e) {
			throw new RpcException(e);
		}
	}
	@Override
	public boolean isSingleton() {
		return true;
	}
	
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
	public Protocal getProtocal() {
		return protocal;
	}
	public void setProtocal(Protocal protocal) {
		this.protocal = protocal;
	}
	public Serializer getSerializer() {
		return serializer;
	}
	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}
	public int getInvokeTimeout() {
		return invokeTimeout;
	}
	public void setInvokeTimeout(int invokeTimeout) {
		this.invokeTimeout = invokeTimeout;
	}
	public InvokePipeline getPipeline() {
		return pipeline;
	}
	public void setPipeline(InvokePipeline pipeline) {
		this.pipeline = pipeline;
	}
}
