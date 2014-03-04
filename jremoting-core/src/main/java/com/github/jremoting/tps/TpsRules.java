package com.github.jremoting.tps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;
import com.github.jremoting.core.Invoke;

public class TpsRules {
	
	public static TpsRules parseJsonRule(String rule) {

		try {
			TpsRules result  = JSON.parseObject(rule, TpsRules.class);
			return result;
		}
		catch(Exception e) {
			return new TpsRules();
		}
	}
	
	public TpsRule getTpsRule(Invoke invoke) {
		for (ServiceRule serviceRule : this.serviceRules) {
			if(serviceRule.isMatch(invoke.getServiceName())) {
				serviceRule.resetCounter();
				return serviceRule;
			}
		}
		return TpsRule.DUMMY;
	}

	private List<ServiceRule> serviceRules = new ArrayList<ServiceRule>();
	
	public static abstract class TpsRule {

		private int rate;
		private long timeWindow;
		private TpsCounter counter;
		private int peak;
		
		public  void resetCounter() {
			if(rate > 0 && timeWindow > 0 && peak > 0) {
				 this.counter = new TpsCounter(timeWindow, rate,peak);
			}
			else {
				this.counter = null;
			}
		}
		
		public boolean beginInvoke(Invoke invoke) {
			return counter == null ? true : counter.beginInvoke();
		}
		
		public void endInvoke(Invoke invoke) {
			if(counter != null) {
				counter.endInvoke();
			}
		}

		public int getRate() {
			return rate;
		}
		public void setRate(int rate) {
			this.rate = rate;
		}
		public long getTimeWindow() {
			return timeWindow;
		}
		public void setTimeWindow(long timeWindow) {
			this.timeWindow = timeWindow;
		}
		
		public int getPeak() {
			return peak;
		}
		public void setPeak(int peak) {
			this.peak = peak;
		}
		public static TpsRule DUMMY = new TpsRule() {
			public boolean beginInvoke(Invoke invoke) {
				return true;
			}
			public void endInvoke(Invoke invoke) {
				
			}
		};
	}
	
	public static class ServiceRule extends TpsRule {
	
		private List<MethodRule> methodRules;
		private ConcurrentHashMap<String, TpsRule> cachedMethodRules = new ConcurrentHashMap<String,TpsRule>();
		private String serviceName;
		
		public List<MethodRule> getMethodRules() {
			return methodRules;
		}
		public void setMethodRules(List<MethodRule> methodRules) {
			this.methodRules = methodRules;
		}
		public String getServiceName() {
			return serviceName;
		}
		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}
		public boolean isMatch(String serviceName) {
			return this.serviceName.equals(serviceName);
		}
		
		
		public boolean beginInvoke(Invoke invoke) {
			if(!super.beginInvoke(invoke)) {
				return false;
			}
			
			TpsRule methodRule = getMethodRule(invoke.getMethodName());
			
			return methodRule.beginInvoke(invoke);
		}
		
		public void endInvoke(Invoke invoke) {
			super.endInvoke(invoke);
			
			TpsRule methodRule = getMethodRule(invoke.getMethodName());
			
			methodRule.endInvoke(invoke);
		}
		
		private TpsRule getMethodRule(String methodName) {
			TpsRule methodRule = cachedMethodRules.get(methodName);
			if(methodRule == null) {
				for (MethodRule rule : this.methodRules) {
					if(rule.isMatch(methodName)) {
						methodRule.resetCounter();
						methodRule = rule;
					}
				}
				
				if(methodRule == null) {
					methodRule = TpsRule.DUMMY;
				}
				
				cachedMethodRules.put(methodName, methodRule);
			}
			
			return methodRule;
		}
	}
	
	public static class MethodRule extends TpsRule {
		private String methodName;

		public String getMethodName() {
			return methodName;
		}

		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}
		
		public boolean isMatch(String methodName) {
			return this.methodName.equals(methodName);
		}
		
		public boolean beginInvoke(Invoke invoke) {
			return false;
		}
	}

	public List<ServiceRule> getServiceRules() {
		return serviceRules;
	}

	public void setServiceRules(List<ServiceRule> serviceRules) {
		this.serviceRules = serviceRules;
	}
}
