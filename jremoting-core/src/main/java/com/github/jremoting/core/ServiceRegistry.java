package com.github.jremoting.core;

import java.util.List;

public interface ServiceRegistry {
	List<ServiceProviderInfo> getProviderInfos(String serviceId);
}
