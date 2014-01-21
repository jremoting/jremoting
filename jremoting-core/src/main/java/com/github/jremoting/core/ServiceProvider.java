package com.github.jremoting.core;

public interface ServiceProvider {
	InvokePipeline getPipeline();
	Object getTarget();
	String getService();
}
