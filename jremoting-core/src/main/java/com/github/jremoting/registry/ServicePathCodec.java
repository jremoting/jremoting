package com.github.jremoting.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.github.jremoting.core.ServiceParticipantInfo;
import com.github.jremoting.core.ServiceParticipantInfo.ParticipantType;

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
	
	public String toServicePath(ServiceParticipantInfo participant) {
		if(participant.getType() == ParticipantType.PROVIDER) {
			return "/" + participant.getServiceName() + "/providers/" + JSON.toJSONString(participant);
		}
		else {
			return "/" + participant.getServiceName() + "/consumers/" + JSON.toJSONString(participant); 
		}
	}
	
	public String toProvidersDir(String serviceName) {
		String providersPath = String.format("/%s/providers", serviceName);
		return providersPath;
	}
	
	public Map<String, List<ServiceParticipantInfo>> parseChangedProviderPath(String changedParentPath, List<String> providerFileNames) {
		HashMap<String, List<ServiceParticipantInfo>> participants = new HashMap<String, List<ServiceParticipantInfo>>();
		String serviceName = parseServiveName(changedParentPath);
		List<ServiceParticipantInfo> providers = new ArrayList<ServiceParticipantInfo>();
		for (String json : providerFileNames) {
			providers.add(JSON.parseObject(json, ServiceParticipantInfo.class));
		}
		
		participants.put(serviceName, providers);
		return participants;
	}
	
	private String parseServiveName(String path) {
		// path = /serviceName/providers
		return path.substring(1, path.indexOf("/providers"));
	}

}
