package com.github.jremoting.core;

public class ServiceParticipantInfo {

	public static enum ParticipantType {
		PROVIDER,CONSUMER
	}
		
	private  String address;
	private  String serviceName;
	private  ParticipantType type;
	
	public ServiceParticipantInfo(){}
	
	public ServiceParticipantInfo(String serviceName,String address,ParticipantType type) {
		this.serviceName = serviceName;
		this.address = address;
		this.type = type;
	}
	
	public String getAddress() {
		return address;
	}
	public String getServiceName() {
		return serviceName;
	}

	public ParticipantType getType() {
		return type;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setType(ParticipantType type) {
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		return serviceName.hashCode() + address.hashCode() + type.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ServiceParticipantInfo) {
			ServiceParticipantInfo that = (ServiceParticipantInfo)obj;
			return this.serviceName.equals(that.serviceName) &&
					this.address.equals(that.address) &&
					this.type == that.type;
		}
		
		return false;
	}

}
