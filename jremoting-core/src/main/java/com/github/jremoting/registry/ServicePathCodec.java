package com.github.jremoting.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




import com.github.jremoting.core.ServiceParticipant;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.util.JsonUtil;

public class ServicePathCodec {
	
	public String getRootPath() {
		return "jremoting";
	}
	
	public String[] getServiceDirs(String serviceName) {
		return new String[] { 
				"/" + serviceName,
				"/" + serviceName + "/providers",
				"/" + serviceName + "/consumers"
		};
	}
	
	public String toServicePath(ServiceParticipant participant) {
		if(participant instanceof ServiceProvider) {
			return "/" + participant.getServiceName() + "/providers/" + JsonUtil.toJson(participant);
		}
		else {
			return "/" + participant.getServiceName() + "/consumers/" + JsonUtil.toJson(participant); 
		}
	}
	
	public String toProvidersDir(String serviceName) {
		String providersPath = String.format("/%s/providers", serviceName);
		return providersPath;
	}
	
	public Map<String, List<ServiceParticipant>> parseChangedProviderPath(String changedParentPath, List<String> providerFileNames) {
		HashMap<String, List<ServiceParticipant>> participants = new HashMap<String, List<ServiceParticipant>>();
		String serviceName = parseServiveName(changedParentPath);
		List<ServiceParticipant> providers = new ArrayList<ServiceParticipant>();
		for (String json : providerFileNames) {
			providers.add(JsonUtil.fromJson(json, ServiceParticipant.class));
		}
		
		participants.put(serviceName, providers);
		return participants;
	}
	
	private String parseServiveName(String path) {
		// path = /serviceName/providers
		return path.substring(1, path.indexOf("/providers"));
	}

}
