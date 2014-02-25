package com.github.jremoting.core;

import java.util.List;

public class RegistryEvent {
	public enum EventType {
		PROVIDERS_CHANGED,
		GLOBAL_CONFIG_CHANGED,
		APP_CONFIG_CHANGED,
		SERVICE_CONFIG_CHANGED,
		RECOVER
	}
	
	private EventType type;
	private String appName;
	private String serviceName;
	private String fileName;
	private String newContent;
	
	
	private List<ServiceProvider> newProviders;
	private String serviceId;
	
	public EventType getType() {
		return type;
	}
	public void setType(EventType type) {
		this.type = type;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getNewContent() {
		return newContent;
	}
	public void setNewContent(String newContent) {
		this.newContent = newContent;
	}
	public List<ServiceProvider> getNewProviders() {
		return newProviders;
	}
	public void setNewProviders(List<ServiceProvider> newProviders) {
		this.newProviders = newProviders;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

}
