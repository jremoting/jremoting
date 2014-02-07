package com.github.jremoting.remoting;


import java.util.concurrent.Executor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.RpcServer;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.invoker.ServerRpcInvoker;

public class DefaultRpcServer implements RpcServer {
	
	private volatile boolean started = false;
	private final EventLoopGroup parentGroup;
	private final EventLoopGroup childGroup;
	private int port = 8686;
	private String ip = "127.0.0.1"; //TODO get local ip
	private final Protocal protocal;
	private final Executor executor;
	private final ServerRpcInvoker serverRpcInvoker;

	public DefaultRpcServer(EventLoopGroup parentGroup, 
			EventLoopGroup childGroup,
			Executor executor,
			Protocal protocal, 
			ServerRpcInvoker serverRpcInvoker) {
		this.executor = executor;
		this.parentGroup = parentGroup;
		this.childGroup = childGroup;
		this.protocal = protocal;
		this.serverRpcInvoker = serverRpcInvoker;
	}

	@Override
	public void start() {
		if(started) {
			return;
		}
		
		synchronized (this) {
			if(started) {
				return;
			}
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(parentGroup, childGroup)
			.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new NettyMessageCodec(protocal),
							new NettyServerHandler(executor,serverRpcInvoker));
				}
			});
			
			try {
				bootstrap.bind(ip ,port).sync();
			} catch (InterruptedException e) {
				throw new RemotingException("jremmoting can not bind to local address:" + ip + ":"+ port);
			}
			started = true;
		}
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
