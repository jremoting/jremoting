package com.github.jremoting.weight;

import java.util.ArrayList;
import java.util.List;

import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.util.JsonUtil;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;
import com.github.jremoting.util.StringUtil;

public class WeightStrategy {
	
	private  WeightRule weightRule;
	
	private final Logger logger = LoggerFactory.getLogger(WeightStrategy.class);
	
	public WeightStrategy(String rule) {
		if(StringUtil.isEmpty(rule)) {
			this.weightRule = null;
		}
		else {
			try {
				this.weightRule = JsonUtil.fromJson(rule, WeightRule.class);
			} catch (Exception e) {
				logger.error("invalid weight json:" + e.getMessage(),e);
				this.weightRule = null;
			}
		}
		
	}
	
	public List<ServiceProvider> applyWeightRule(List<ServiceProvider> originalProviders) {
		if(weightRule == null) {
			return originalProviders;
		}
		
		List<ServiceProvider> outputProviders = new ArrayList<ServiceProvider>();
		for (ServiceProvider serviceProvider : originalProviders) {
			for (int i = 0; i < weightRule.getWeight(serviceProvider.getIp()); i++) {
				outputProviders.add(serviceProvider);
			}
		}
		return outputProviders;
	}
	
}
