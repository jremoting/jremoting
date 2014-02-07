package com.github.jremoting.core;

public class ServiceProviderInfo {

	private final String address;
	private final String serviceId;
	
	public ServiceProviderInfo(String serviceId,String address) {
		this.serviceId = serviceId;
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
	public String getServiceId() {
		return serviceId;
	}
}
