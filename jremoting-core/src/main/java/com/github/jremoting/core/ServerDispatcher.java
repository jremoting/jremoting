package com.github.jremoting.core;

public interface ServerDispatcher {
	void start();
	void registerProvider(ServiceProvider provider);
}
