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
	
	public ServiceParticipant(String interfaceName, String version, String group) {
		this.interfaceName = interfaceName;
		this.version = version;
		if(group == null) {
			this.group = DEFAULT_GROUP;
		}
		else {
			this.group = group;
		}
	}

	
	public String getAddress() {
		return address;
	}
	public String getServiceName() {
		return this.interfaceName + ":" + this.version;
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

}
