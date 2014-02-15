package com.github.jremoting.remoting;

import java.util.List;
import java.util.concurrent.ExecutorService;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import com.github.jremoting.core.InvokeFilter;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.RpcServer;
import com.github.jremoting.core.ServiceParticipantInfo;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.core.ServiceRegistry;
import com.github.jremoting.core.ServiceParticipantInfo.ParticipantType;
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
	private final ExecutorService executor;
	private final ServerInvokeFilterChain invokeFilterChain;
	private final ServiceRegistry registry;
	private final String serverAddress;
	private volatile boolean containsProvider = false;
	private volatile Channel serverChannel;
	private final Logger logger = LoggerFactory.getLogger(DefaultRpcServer.class);
	
	private final LifeCycleSupport lifeCycleSupport = new LifeCycleSupport();

	public DefaultRpcServer(EventLoopGroup parentGroup, 
			EventLoopGroup childGroup,
			ExecutorService executor,
			Protocal protocal, 
			int port,
			List<InvokeFilter> invokeFilters) {
		this.executor = executor;
		this.parentGroup = parentGroup;
		this.childGroup = childGroup;
		this.protocal = protocal;
		this.invokeFilterChain = new ServerInvokeFilterChain(invokeFilters);
		this.registry = protocal.getRegistry();
		this.serverAddress = NetUtil.getLocalHost() + ":" + port;
	}

	@Override
	public void start() {
		
		if(!containsProvider){ 
			return;
		}
		
		logger.info("jremoting rpc server begin to listen address:" + this.serverAddress);
		
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
		.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new NettyMessageCodec(protocal),
						new NettyServerHandler(executor,invokeFilterChain));
			}
		});
		
		
		try {
		 	ChannelFuture future = bootstrap.bind(NetUtil.toInetSocketAddress(serverAddress)).sync();
		 	serverChannel = future.channel();
		 	if(this.registry != null) {
				this.registry.start();
			}
		 	
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
		
		this.serverChannel.close();
		this.parentGroup.shutdownGracefully();
		this.executor.shutdown();
		this.childGroup.shutdownGracefully();
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
}
