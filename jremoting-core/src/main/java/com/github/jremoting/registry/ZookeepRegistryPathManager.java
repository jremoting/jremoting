package com.github.jremoting.registry;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.github.jremoting.core.ServiceParticipant;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.exception.RegistryExcpetion;
import com.github.jremoting.util.URL;

public class ZookeepRegistryPathManager {
	
	public String getNamespace() {
		return "jremoting";
	}
	
	public String getGlobalConfigDir() {
		return "/configs/";
	}
	
	public String getAppConfigDir() {
		return "/apps/";
	}
	
	public String getServiceConfigDir() {
		return "/services/";
	}
	
	public String parseServiceId(String providerDir) {
		String[] paths = providerDir.split("/");
		String serviceId = paths[1];
		return serviceId;
	}
	
	public String getProviderDir(ServiceParticipant participant) {
		return "/" + participant.getServiceId() + "/providers" ;
	}
	
	public String getConsumerDir(ServiceParticipant participant) {
		return "/" + participant.getServiceId() + "/consumers" ;
	}

	public String encode(ServiceParticipant participant) {
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("timeout", String.valueOf(participant.getTimeout()));
		args.put("failover",  String.valueOf(participant.getFailover()));
		args.put("retry",  String.valueOf(participant.getRetry()));
		args.put("version", participant.getVersion());
		args.put("group", participant.getGroup());
		
		String protocol = null;
		
		if(participant instanceof ServiceProvider) {
			protocol = "provider";
		}
		else {
			protocol = "consumer";
		}
		
		URL url = new URL(protocol, participant.getAddress(), participant.getInterfaceName(), args);
		return encodeUrl(url.toString());
	}
	
	private String encodeUrl(String url) {
		try {
			return URLEncoder.encode(url, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RegistryExcpetion("should not happen");
		}
	}
	
	private String decodeUrl(String url) {
		try {
			return URLDecoder.decode(url, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RegistryExcpetion("should not happen");
		}
	}
	
	public ServiceProvider decode(String fileName) {
		URL url = URL.valueOf(decodeUrl(fileName));
		
		ServiceProvider provider = new ServiceProvider(url.getPath(),
				url.getParameter("version"), url.getParameter("group"));
		
		provider.setAddress(url.getAddress());
		String retry = url.getParameter("retry");
		if(retry != null) {
			provider.setRetry(Integer.parseInt(retry));
		}
		
		String failover = url.getParameter("failover");
		if(retry != null) {
			provider.setFailover(Integer.parseInt(failover));
		}
		
		String timeout = url.getParameter("timeout");
		if(timeout != null) {
			provider.setTimeout(Long.parseLong(timeout));
		}
		
		return provider;
	}
}
