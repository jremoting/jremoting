package com.github.jremoting.core.test;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;



public class GrouRuleTest {
	public static void main(String[] args) throws Exception {
		String rule = "{ruleItems : [{serviceNames:['com.*'],ips:['10.*'],group:'dfd'}]}";
		 CuratorFramework client; 
		 RetryPolicy retryPolicy = new RetryNTimes(Integer.MAX_VALUE, 1000);
		 client = CuratorFrameworkFactory.builder()
					.connectString("127.0.0.1:2181")
					.sessionTimeoutMs(5 * 1000).connectionTimeoutMs(5 * 1000).retryPolicy(retryPolicy).build();
		 client.start();
		 
		 client.setData().forPath("/jremoting/apps/test_provider/group.rule", rule.getBytes());
		 client.setData().forPath("/jremoting/apps/test_consumer/group.rule", rule.getBytes());
		 
		// System.in.read();
	}
}
