package com.github.jremoting.core;

import java.util.List;

public interface ServiceRegistry {
	
	List<ServiceProvider> getProviders(Invoke invoke);
	void start();
	void close();
	void publish(ServiceProvider provider);
	void subscribe(ServiceConsumer consumer);

	ServiceConfig getConfig(String serviceName, String key);
	
	public static class ServiceConfig {
		
		private long version;
		private String data;
		private String key;
		
		public long getVersion() {
			return version;
		}
		public void setVersion(long version) {
			this.version = version;
		}
		public String getData() {
			return data;
		}
		public void setData(String data) {
			this.data = data;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
	}
}
