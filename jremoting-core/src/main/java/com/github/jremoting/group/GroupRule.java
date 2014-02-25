package com.github.jremoting.group;

import java.util.List;

import com.github.jremoting.core.ServiceParticipant;

public class GroupRule {
	
	private  List<GroupRuleItem> ruleItems;

	public List<GroupRuleItem> getRuleItems() {
		return ruleItems;
	}

	public void setRuleItems(List<GroupRuleItem> ruleItems) {
		this.ruleItems = ruleItems;
	}
	
	public String getNewGroup(ServiceParticipant participant, String localIp) {
		for (GroupRuleItem item: ruleItems) {
			if(item.isMatch(participant.getServiceName(), localIp)) {
				if(!item.getGroup().equals(participant.getGroup())) {
					return item.getGroup();
				}
			}
		}
		return participant.getGroup();
	}
}
