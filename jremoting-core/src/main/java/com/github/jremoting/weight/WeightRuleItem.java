package com.github.jremoting.weight;

import com.github.jremoting.util.WildcardUtil;

public class WeightRuleItem {
	private String[] ipPatterns;
	private int weight;
	
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public String[] getIpPatterns() {
		return ipPatterns;
	}
	public void setIpPatterns(String[] ipPatterns) {
		this.ipPatterns = ipPatterns;
	}
	
	public boolean isMatch(String ip) {
		for (String pattern : ipPatterns) {
			if(WildcardUtil.equalsOrMatch(ip, pattern)) {
				return true;
			}
		}
		return false;
	}
}
