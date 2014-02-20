package com.github.jremoting.registry;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jremoting.core.ServiceParticipantInfo;
import com.github.jremoting.core.ServiceParticipantInfo.ParticipantType;
import com.github.jremoting.exception.RegistryExcpetion;

public class DubboServiceRegistry extends DefaultServiceRegistry {

	public DubboServiceRegistry(String zookeeperConnectionString) {
		super(zookeeperConnectionString);
		this.codec = new DubboServicePathCodec();
	}
	
	private static class DubboServicePathCodec extends ServicePathCodec {

		@Override
		public String getRootPath() {
			return "dubbo";
		}
		
		@Override
		public String[] getServiceDirs(String serviceName) {
			String interfaceName = serviceName.substring(0,serviceName.indexOf(":"));
			return new String[] { 
					"/" + interfaceName,
					"/" + interfaceName + "/providers",
					"/" + interfaceName + "/consumers"
			};
		}
		
		public String toProvidersDir(String serviceName) {
			String interfaceName = serviceName.substring(0, serviceName.indexOf(":"));
			String providersPath = String.format("/%s/providers", interfaceName);
			return providersPath;
		}
		
		//dubbo://10.10.53.183:20880/com.demo.TestService?anyhost=true&application=demo-server&dubbo=2.5.3&interface=com.demo.TestService
		//&methods=hello&organization=demo&pid=1664&revision=1.0.0&side=provider&timestamp=1392864704647&version=1.0.0
		private String dubboUrlFormat = "dubbo://%s/%s?anyhost=true&interface=%s&revision=%s&side=%s&version=%s";
		
		@Override
		public String toServicePath(ServiceParticipantInfo participant) {
			String interfaceName = participant.getServiceName().substring(0,participant.getServiceName().indexOf(":"));
			String version = participant.getServiceName().substring(participant.getServiceName().indexOf(":")+1, participant.getServiceName().length());
			
			if(participant.getType() == ParticipantType.PROVIDER) {
				String fileName = String.format(dubboUrlFormat, participant.getAddress(),interfaceName,interfaceName,version,"provider",version);

				return "/" + interfaceName + "/providers/" + encode(fileName);
			}
			else {
				String fileName = String.format(dubboUrlFormat, participant.getAddress(),interfaceName,interfaceName,version,"consumer",version );
				return "/" + interfaceName + "/consumers/" +  encode(fileName); 
			}
		}
		
		@Override
		public Map<String, List<ServiceParticipantInfo>> parseChangedProviderPath(String changedParentPath, List<String> providerFileNames) {
			
			HashMap<String, List<ServiceParticipantInfo>> participants = new HashMap<String, List<ServiceParticipantInfo>>();
			
			for (String fileName : providerFileNames) {
				String url = decode(fileName);
				
				String address = parseAddress(url);
				
				String queryString = url.substring(url.indexOf("?") + 1, url.length());
				String[] kvPairs = queryString.split("&");
				String interfaceName = null;
				String version = null;
				
				for (String pair : kvPairs) {
					String[] kv = pair.split("=");
					if("interface".equals(kv[0] )) {
						interfaceName =kv[1];
					}
					if("version".equals(kv[0])) {
						version = kv[1];
					}
				}
				if(interfaceName != null && version != null) {
					String serviceName =interfaceName + ":" + version ;
					List<ServiceParticipantInfo> providers = participants.get(serviceName);
					if(providers == null) {
						providers = new ArrayList<ServiceParticipantInfo>();
						participants.put(serviceName, providers);
					}
					providers.add(new ServiceParticipantInfo(serviceName, address, ParticipantType.PROVIDER));
				}
 			}
		
			return participants;
		}
		
		private String parseAddress(String url) {
			String str = url.replace("dubbo://", "");
			return str.substring(0, str.indexOf("/"));
		}
		
		private String encode(String value) { 
			try {
				return  URLEncoder.encode(value,"utf-8");
			} catch (UnsupportedEncodingException e) {
				throw new RegistryExcpetion("url encode error", e);
			}
		}
		private String decode(String value) { 
			try {
				return  URLDecoder.decode(value,"utf-8");
			} catch (UnsupportedEncodingException e) {
				throw new RegistryExcpetion("url decode error", e);
			}
		}
	}

}
