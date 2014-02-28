package com.github.jremoting.core;

public abstract class ServiceParticipant {

	public static final String DEFAULT_GROUP = "default";
	public static final String DEFAULT_APP_NAME = System.getProperty("jremoting.app.name","jremoting");
	
	private String appName = DEFAULT_APP_NAME;
	private final String interfaceName;
	private final String version;
	private String group;
	
	private String address;
	private long timeout;
	private int retry;
	private int failover;
	
	private final String serviceName;
	private String serviceId;
	
	private boolean devMode =Boolean.parseBoolean(System.getProperty("jremoting.dev.mode", "false"));
	
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

	public String getServiceName() {
		return serviceName;
	}
	
	public String getServiceId() { 
		return serviceId;
	}
	
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
		this.serviceId = this.serviceName + ":" + this.group;
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

	public String getVersion() {
		return version;
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

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public boolean isDevMode() {
		return devMode;
	}

	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}

}
