package com.github.jremoting.remoting;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import com.github.jremoting.core.InvokeFilter;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.RpcServer;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.core.Registry;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.invoke.ServerInvokeFilterChain;
import com.github.jremoting.util.LifeCycleSupport;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;
import com.github.jremoting.util.NetUtil;
import com.github.jremoting.util.concurrent.EventExecutor;

public class DefaultRpcServer implements RpcServer {

	private final EventLoopGroup parentGroup;
	private final EventLoopGroup childGroup;
	private final Protocal protocal;
	private final ExecutorService serviceExecutor;
	private final ServerInvokeFilterChain invokeFilterChain;
	private final Registry registry;
	private final String serverAddress;
	private volatile Channel serverChannel;
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRpcServer.class);
	
	private final LifeCycleSupport lifeCycleSupport = new LifeCycleSupport();
	private final Map<String, ServiceProvider> providers = new ConcurrentHashMap<String, ServiceProvider>();

	public DefaultRpcServer(EventExecutor eventExecutor,
			ExecutorService serviceExecutor,
			Protocal protocal, 
			int port,
			List<InvokeFilter> invokeFilters) {
		this.serviceExecutor = serviceExecutor;
		this.parentGroup = eventExecutor.getParentGroup();
		this.childGroup = eventExecutor.getChildGroup();
		this.protocal = protocal;
		this.invokeFilterChain = new ServerInvokeFilterChain(invokeFilters);
		this.registry = protocal.getRegistry();
		this.serverAddress = NetUtil.getLocalIp() + ":" + port;
	}

	@Override
	public void start() {
		lifeCycleSupport.start(new Runnable() {
			@Override
			public void run() {
				doStart();
			}
		});
	}

	private void doStart() {
	
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(parentGroup, childGroup)
				.channel(NioServerSocketChannel.class)
				.childOption(ChannelOption.TCP_NODELAY, true)
				.childOption(ChannelOption.SO_KEEPALIVE, true)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(
								new NettyMessageCodec(protocal),
								new NettyServerHandler(serviceExecutor,invokeFilterChain, providers));
					}
				});
		
		
		try {
		 	ChannelFuture future = bootstrap.bind(NetUtil.toInetSocketAddress(serverAddress)).sync();
		 	serverChannel = future.channel();
		 	
			LOGGER.info("jremoting rpc server begin to listen address:" + this.serverAddress);
		 	
		} catch (InterruptedException e) {
			throw new RemotingException("jremmoting can not bind to local address:" + this.serverAddress);
		}
	}

	@Override
	public void close() {
		lifeCycleSupport.close(new Runnable() {
			@Override
			public void run() {
				doClose();
			}
		});
	}

	private void doClose() {
		if(this.registry != null) {
			this.registry.close();
		}
		//close server channel and server channel io thread to refuse new connection
		if(this.serverChannel != null) {
			this.serverChannel.close();
		}
		
		this.parentGroup.shutdownGracefully();
		//shutdown service executor thread pool refuse new invoke
		this.serviceExecutor.shutdown();
		//
		this.childGroup.shutdownGracefully();
		LOGGER.info("jremoting rpc server closed normally");
	}

	@Override
	public void register(ServiceProvider provider) {
		this.providers.put(provider.getServiceName(), provider);
		provider.setAddress(serverAddress);
		this.start();
		if(this.registry != null) {
			this.registry.publish(provider);
		}
	}
}
