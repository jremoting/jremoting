package com.github.jremoting.remoting;

import java.util.concurrent.ExecutorService;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.RpcServer;
import com.github.jremoting.core.ServiceParticipantInfo;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.core.ServiceRegistry;
import com.github.jremoting.core.ServiceParticipantInfo.ParticipantType;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.invoke.ServerInvokeFilterChain;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;
import com.github.jremoting.util.NetUtil;

public class DefaultRpcServer implements RpcServer, ApplicationListener<ApplicationEvent> {
	
	private volatile boolean started = false;
	private final EventLoopGroup parentGroup;
	private final EventLoopGroup childGroup;
	private final Protocal protocal;
	private final ExecutorService executor;
	private final ServerInvokeFilterChain invokeFilterChain;
	private final ServiceRegistry registry;
	private final String serverAddress;
	private volatile boolean containsProvider = false;
	private volatile Channel serverChannel;
	private final Logger logger = LoggerFactory.getLogger(DefaultRpcServer.class);

	public DefaultRpcServer(EventLoopGroup parentGroup, 
			EventLoopGroup childGroup,
			ExecutorService executor,
			Protocal protocal, 
			int port,
			ServerInvokeFilterChain invokeFilterChain, ServiceRegistry registry) {
		this.executor = executor;
		this.parentGroup = parentGroup;
		this.childGroup = childGroup;
		this.protocal = protocal;
		this.invokeFilterChain = invokeFilterChain;
		this.registry = registry;
		this.serverAddress = NetUtil.getLocalHost() + ":" + port;
	}

	@Override
	public void start() {
		
		logger.info("jremoting rpc server begin to listen address:" + this.serverAddress);
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
			
			
			try {
			 	ChannelFuture future = bootstrap.bind(NetUtil.toInetSocketAddress(serverAddress)).sync();
			 	serverChannel = future.channel();
			} catch (InterruptedException e) {
				throw new RemotingException("jremmoting can not bind to local address:" + this.serverAddress);
			}
			started = true;
		}
	}

	@Override
	public void close() {
		if(started) {
			this.serverChannel.close();
			this.parentGroup.shutdownGracefully();
			this.executor.shutdown();
			this.childGroup.shutdownGracefully();
		}
	}

	@Override
	public void register(ServiceProvider provider) {
		this.invokeFilterChain.register(provider);
		this.registry.registerParticipant(new ServiceParticipantInfo(provider.getServiceName(),
				this.serverAddress, ParticipantType.PROVIDER));
		if(!containsProvider) {
			containsProvider = true;
		}
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if(event instanceof ContextRefreshedEvent) {
			if(containsProvider){
				this.start();
				this.registry.start();
			}
		}
		else if(event instanceof ContextClosedEvent) {
			if(started) {
				this.registry.close();
				this.close();
			}
		}
	}

}
