package com.github.jremoting.group;

import com.alibaba.fastjson.JSON;
import com.github.jremoting.core.ServiceParticipant;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;
import com.github.jremoting.util.StringUtil;

public class GroupStrategy {
	
	private final String localIp;
	private  GroupRule appGroupRule;
	private  GroupRule serviceGroupRule;
	private  final String appName;
	private  final String serviceName;
	private final String appConfig;
	private final String serviceConfig;
	
	private final Logger logger = LoggerFactory.getLogger(GroupStrategy.class);
	
	public GroupStrategy(String appName,String appConfig,String serviceName ,String serviceConfig, String localIp) {
		
		this.localIp = localIp;
		this.appName = appName;
		this.serviceName = serviceName;
		this.appConfig = appConfig;
		this.serviceConfig = serviceConfig;
		
		if (StringUtil.isNotEmpty(this.appConfig)) {
			try {
				this.appGroupRule = JSON.parseObject(this.appConfig,
						GroupRule.class);
			} catch (Exception e) {
				logger.error("parse app group rule failed appName:"
						+ this.appName + "\n" + this.appConfig);
			}
		}

		if (StringUtil.isNotEmpty(this.serviceConfig)) {
			try {
				this.serviceGroupRule = JSON.parseObject(this.appConfig,
						GroupRule.class);
			} catch (Exception e) {
				logger.error("parse service group rule failed serviceName:"
						+ this.serviceName + "\n" + this.serviceConfig);
			}
		}
		
	}

	public String getNewGroup(ServiceParticipant participant) {
		if(this.serviceGroupRule != null) {
			String newGroup = this.serviceGroupRule.getNewGroup(participant, localIp);
			if(!newGroup.equals(participant.getGroup())) {
				return newGroup;
			}
		}
		
		if(this.appGroupRule != null) {
			String newGroup = this.appGroupRule.getNewGroup(participant, localIp);
			if(!newGroup.equals(participant.getGroup())) {
				return newGroup;
			}
		}
		
		return participant.getGroup();
	}
}
