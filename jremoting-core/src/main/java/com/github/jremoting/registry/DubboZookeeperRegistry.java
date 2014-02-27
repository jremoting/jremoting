package com.github.jremoting.registry;


import java.util.HashMap;
import java.util.Map;

import com.github.jremoting.core.ServiceParticipant;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.util.URL;



public class DubboZookeeperRegistry extends ZookeeperRegistry {

	public DubboZookeeperRegistry(String zookeeperConnectionString) {
		super(zookeeperConnectionString);
		this.pathManager = new DubboRegistryPathManager();
	}
	
	private static class DubboRegistryPathManager extends RegistryPathManager {
		public String getNamespace() {
			return "dubbo";
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
		
		public String getProviderDir(ServiceParticipant participant) {
			return "/" + participant.getInterfaceName() + "/providers" ;
		}
		
		public String getConsumerDir(ServiceParticipant participant) {
			return "/" + participant.getInterfaceName() + "/consumers" ;
		}

		public String encode(ServiceParticipant participant) {
			
			Map<String, String> args = new HashMap<String, String>();
			args.put("timeout", String.valueOf(participant.getTimeout()));
			args.put("failover",  String.valueOf(participant.getFailover()));
			args.put("retries",  String.valueOf(participant.getRetry()));
			args.put("version", participant.getVersion());
			
			if(!ServiceParticipant.DEFAULT_GROUP.equals(participant.getGroup())) {
				args.put("group", participant.getGroup());
			}
			args.put("interface", participant.getInterfaceName());
			
			if(participant instanceof ServiceProvider) {
				args.put("side", "provider");
			}
			else {
				args.put("side", "consumer");
			}
			
			
			String protocol = "dubbo";
			
			URL url = new URL(protocol, participant.getAddress(), participant.getInterfaceName(), args);
			return encodeUrl(url.toString());
		}
		
		public ServiceProvider decode(String fileName) {
			URL url = URL.valueOf(decodeUrl(fileName));
			
			
			ServiceProvider provider = new ServiceProvider(url.getPath(),
					url.getParameter("version"), url.getParameter("group"));
			
			provider.setAddress(url.getAddress());
			String retry = url.getParameter("retries");
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

}
