package com.github.jremoting.dispatcher;

import java.util.List;
import java.util.concurrent.Executor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;

import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.InvokePipeline;
import com.github.jremoting.core.ServerDispatcher;
import com.github.jremoting.exception.RpcException;
import com.github.jremoting.protocal.Protocals;

public class JRemotingServerDispatcher implements ServerDispatcher {

	private volatile boolean started = false;
	private final EventLoopGroup parentGroup;
	private final EventLoopGroup childGroup;
	private final Protocals protocals;
	private final Executor executor;
	private final InvokePipeline pipeline;
	public JRemotingServerDispatcher(EventLoopGroup parentGroup,
			EventLoopGroup childGroup,
			Protocals protocals,
			Executor executor, InvokePipeline pipeline) {
		this.parentGroup = parentGroup;
		this.childGroup = childGroup;
		this.protocals = protocals;
		this.executor = executor;
		this.pipeline = pipeline;
		
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
					
					ch.pipeline().addLast(new NettyServerEncoder(), new NettyServerDecoder(),
							new NettyServerHandler());
				}
			});
			
			try {
				bootstrap.bind().sync();
			} catch (InterruptedException e) {
				throw new RpcException("start failed!");
			}
			
			started = true;
		}
	}
	
	private  class NettyServerEncoder extends MessageToByteEncoder<InvocationResult> {

		@Override
		protected void encode(ChannelHandlerContext ctx, InvocationResult msg,
				ByteBuf out) throws Exception {
			
			Invocation invocation = msg.getInvocation();
			invocation.getProtocal().writeResponse(invocation, msg, new JRemotingChannelBuffer(out));
		}
	}
	
	private  class NettyServerDecoder extends ReplayingDecoder<Invocation> {

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in,
				List<Object> out) throws Exception {
			
			Invocation invocation = protocals.readRequest(new JRemotingChannelBuffer(in));
			if(invocation != null) {
				out.add(invocation);
			}
			
		}
	}
	
	private  class NettyServerHandler extends ChannelInboundHandlerAdapter {
		
		@Override
		public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
			if(msg instanceof Invocation) {
				executor.execute(new Runnable() {
					
					@Override
					public void run() {
						Invocation invocation = (Invocation)msg;
						Object result = pipeline.invoke(invocation);
						
						InvocationResult invocationResult = new InvocationResult(invocation.getInvocationId(), result);
						
						ctx.channel().write(invocationResult);
					}
				});
			}
		}
	}



}
