package com.github.jremoting.core;

public class ServiceProviderInfo {

	private final String address;
	private final String serviceName;
	
	public ServiceProviderInfo(String serviceName,String address) {
		this.serviceName = serviceName;
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
	public String getServiceName() {
		return serviceName;
	}
}
