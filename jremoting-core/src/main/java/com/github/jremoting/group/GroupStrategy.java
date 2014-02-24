package com.github.jremoting.group;

import java.util.List;

import com.github.jremoting.core.ServiceParticipant;

public class GroupStrategy {
	private  List<GroupRule> groupRules;
	private  String localIp;
	
	public GroupStrategy(String appConfig, String serviceConfig, String localIp) {
		this.localIp = localIp;
	}
	
	public String getNewGroup(ServiceParticipant participant) {
		for (GroupRule rule: groupRules) {
			if(rule.isMatch(participant.getServiceName(), localIp)) {
				if(!rule.getGroup().equals(participant.getGroup())) {
					return rule.getGroup();
				}
			}
		}
		return participant.getGroup();
	}
}
