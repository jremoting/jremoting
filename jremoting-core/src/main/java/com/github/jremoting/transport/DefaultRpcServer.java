package com.github.jremoting.transport;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
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

public class DefaultRpcServer implements RpcServer {

	private final EventLoopGroup parentGroup;
	private final EventLoopGroup childGroup;
	private final Protocal protocal;
	private final ExecutorService serviceExecutor;
	private final ServerInvokeFilterChain invokeFilterChain;
	private final Registry registry;
	private final String serverAddress;
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
								new NettyServerHandler(serviceExecutor,invokeFilterChain, registry,providers));
					}
				});
		
		
		try {
		 	bootstrap.bind(NetUtil.toInetSocketAddress(serverAddress)).sync();
		 	
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
	
		try {
			this.parentGroup.shutdownGracefully().sync();
		} catch (InterruptedException e) {
			//ignore
		}
		//shutdown service executor thread pool refuse new invoke
		this.serviceExecutor.shutdown();
		try {
			this.serviceExecutor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException ignore) {
		}
		
		Collection<ServiceProvider> localProviders = null;
		if(this.registry != null) {
			localProviders = this.registry.getLocalProviders().values();
		}
		else {
			localProviders = this.providers.values();
		}
		
		for (ServiceProvider provider : localProviders) {
			if(provider.getExecutor() != null) {
				provider.getExecutor().shutdown();
				try {
					provider.getExecutor().awaitTermination(15, TimeUnit.SECONDS);
				} catch (InterruptedException ignore) {
				}
			}
		}
		//
		try {
			this.childGroup.shutdownGracefully().sync();
		} catch (InterruptedException e) {
			//ignore
		}
		LOGGER.info("jremoting rpc server closed normally");
	}

	@Override
	public void register(ServiceProvider provider) {
		provider.setAddress(serverAddress);
		this.start();
		
		if(provider.isDevMode()) {
			providers.put(provider.getServiceId(), provider);
			return;
		}
		
		if(this.registry != null) {
			this.registry.start();
			this.registry.publish(provider);
		}
		else {
			providers.put(provider.getServiceId(), provider);
		}
	}
}
