package com.github.jremoting.util.concurrent;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class EventExecutor {
	private final EventLoopGroup parentGroup = new NioEventLoopGroup();
	private final EventLoopGroup childGroup = new NioEventLoopGroup();
	
	public EventLoopGroup getChildGroup() {
		return childGroup;
	}
	public EventLoopGroup getParentGroup() {
		return parentGroup;
	}
}
