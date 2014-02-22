package com.github.jremoting.group;

import java.util.List;
import com.github.jremoting.util.WildcardUtil;

public class GroupRule {

	private List<String> serviceNames;
	private List<String> ips;
	private String group;

	public List<String> getIps() {
		return ips;
	}
	public void setIps(List<String> ips) {
		this.ips = ips;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public boolean isMatch(String serviceName, String localIp) {
		
		boolean matchServiceName = false;
		for (String name: serviceNames) {
			if(name.equals(serviceName)) {
				matchServiceName = true;
			}
		}
		
		if(!matchServiceName) {
			return false;
		}
		
		for(String ipPattern: ips) {
			if (WildcardUtil.equalsOrMatch(localIp, ipPattern)) {
				return true;
			}
		}
		
		return false;
	}
	public List<String> getServiceNames() {
		return serviceNames;
	}
	public void setServiceNames(List<String> serviceNames) {
		this.serviceNames = serviceNames;
	}
}
