package com.github.jremoting.weight;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;



public class WeightRule {
	
	private List<WeightRuleItem> ruleItems;
	
	private ConcurrentHashMap<String, Integer> cachedWeightIps = new ConcurrentHashMap<String, Integer>();

	public List<WeightRuleItem> getRuleItems() {
		return ruleItems;
	}

	public void setRuleItems(List<WeightRuleItem> ruleItems) {
		this.ruleItems = ruleItems;
	}
	
	public int getWeight(String ip) {
		Integer weight =  cachedWeightIps.get(ip);
		if(weight!=null) {
			return weight;
		}
		
		for (WeightRuleItem item : ruleItems) {
			if(item.isMatch(ip)) {
				cachedWeightIps.put(ip, item.getWeight());
				return item.getWeight();
			}
		}
		cachedWeightIps.put(ip, 1);
		return 1;
	}
	
}
