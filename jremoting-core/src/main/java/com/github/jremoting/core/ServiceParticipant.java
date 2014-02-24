package com.github.jremoting.core;

public abstract class ServiceParticipant {

	public static final String DEFAULT_GROUP = "default";
	
	private String interfaceName;
	private String version;
	private String group;
	
	private String address;
	private long timeout;
	private int retry;
	private int failover;
	
	private String serviceName;
	private String serviceId;
	
	public String getServiceId() {
		return serviceId;
	}


	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}


	public ServiceParticipant(String interfaceName, String version, String group) {
		this.interfaceName = interfaceName;
		this.version = version;
		if(group == null) {
			this.group = DEFAULT_GROUP;
		}
		else {
			this.group = group;
		}
		
		this.serviceName = this.interfaceName + ":" + this.version;
		this.serviceId = this.serviceName + ":" + this.group;
	}

	
	public String getAddress() {
		return address;
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public int getFailover() {
		return failover;
	}

	public void setFailover(int failover) {
		this.failover = failover;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	@Override
	public boolean equals(Object obj)  {
		if(obj == null || !(obj instanceof ServiceParticipant)) {
			return false;
		}
		ServiceParticipant that = (ServiceParticipant)obj;
		
		return this.serviceId.equals(that.serviceId);
	}
	@Override
	public  int hashCode() {
		return this.serviceId.hashCode();
	}

}
