package com.github.jremoting.remoting;


import java.util.concurrent.Executor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.RpcServer;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.invoke.ServerInvokeFilterChain;
import com.github.jremoting.util.NetUtil;

public class DefaultRpcServer implements RpcServer {
	
	private volatile boolean started = false;
	private final EventLoopGroup parentGroup;
	private final EventLoopGroup childGroup;
	private final int port;
	private final String ip;
	private final Protocal protocal;
	private final Executor executor;
	private final ServerInvokeFilterChain invokeFilterChain;

	public DefaultRpcServer(EventLoopGroup parentGroup, 
			EventLoopGroup childGroup,
			Executor executor,
			Protocal protocal, 
			int port,
			String ip,
			ServerInvokeFilterChain invokeFilterChain) {
		this.executor = executor;
		this.parentGroup = parentGroup;
		this.childGroup = childGroup;
		this.protocal = protocal;
		this.port = port;
		this.ip = ip;
		this.invokeFilterChain = invokeFilterChain;
	}
	
	public DefaultRpcServer(EventLoopGroup parentGroup, 
			EventLoopGroup childGroup,
			Executor executor,
			Protocal protocal, 
			int port,
			ServerInvokeFilterChain invokeFilterChain) {
		this(parentGroup, childGroup, executor, protocal, port, null, invokeFilterChain);

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
							new NettyServerHandler(executor,invokeFilterChain));
				}
			});
			
			
			String host = this.ip;
			if(host == null) {
				host = NetUtil.getLocalHost();
			}
			
			try {
				bootstrap.bind(host ,port).sync();
			} catch (InterruptedException e) {
				throw new RemotingException("jremmoting can not bind to local address:" + host + ":"+ port);
			}
			started = true;
		}
	}
	
	public int getPort() {
		return port;
	}

	@Override
	public void stop() {
		parentGroup.shutdownGracefully();
		childGroup.shutdownGracefully();
	}

	@Override
	public void register(ServiceProvider provider) {
		this.invokeFilterChain.register(provider);
	}

}
