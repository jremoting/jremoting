package com.github.jremoting.core;

import java.util.concurrent.Executor;

public interface  ServiceProvider   {
	 Object getTarget();
	 String getServiceName();
	 Executor getExecutor();
}
