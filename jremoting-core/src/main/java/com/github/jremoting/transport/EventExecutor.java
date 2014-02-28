package com.github.jremoting.transport;

import com.github.jremoting.util.concurrent.NamedThreadFactory;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class EventExecutor {
	private final EventLoopGroup parentGroup = new NioEventLoopGroup(0,new NamedThreadFactory("JRemoting-IO", true));
	private final EventLoopGroup childGroup = new NioEventLoopGroup(0,new NamedThreadFactory("JRemoting-IO", true));
	
	public EventLoopGroup getChildGroup() {
		return childGroup;
	}
	public EventLoopGroup getParentGroup() {
		return parentGroup;
	}
}
