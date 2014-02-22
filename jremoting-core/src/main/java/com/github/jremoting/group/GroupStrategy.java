package com.github.jremoting.group;

import java.util.List;

import com.github.jremoting.core.ServiceParticipant;

public class GroupStrategy {
	private final List<GroupRule> groupRules;
	private final String localIp;
	public GroupStrategy(List<GroupRule> groupRules, String localIp) {
		this.groupRules = groupRules;
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
