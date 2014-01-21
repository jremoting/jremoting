package com.github.jremoting.core.test;

import junit.framework.Assert;
import io.netty.channel.embedded.EmbeddedChannel;

import org.junit.Test;

import com.github.jremoting.core.DefaultInvocation;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationHolder;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.Protocal.Pong;
import com.github.jremoting.core.Serializer;
import com.github.jremoting.core.Protocal.Ping;
import com.github.jremoting.dispatcher.NettyClientCodec;
import com.github.jremoting.dispatcher.NettyServerCodec;
import com.github.jremoting.protocal.JRemotingProtocal;
import com.github.jremoting.protocal.Protocals;
import com.github.jremoting.serializer.JsonSerializer;
import com.github.jremoting.serializer.Serializers;

public class JRemotingProtocalTest {

	private Serializer serializer = new JsonSerializer();
	private Serializers serializers  = new Serializers(new Serializer[]{ serializer});
	private JRemotingProtocal protocal = new JRemotingProtocal(serializers);
	private Protocals protocals = new Protocals(new Protocal[]{protocal});
	
	private NettyClientCodec clientCodec = new NettyClientCodec(protocal, null);
	private NettyServerCodec serverCodec = new NettyServerCodec(protocals);



	@Test
	public void testPing() {
		Object obj= clientToServer(protocal.getPing());
		Assert.assertTrue(obj instanceof Ping);
	}
	
	@Test
	public void testPong() {
		Object obj = serverToClient(protocal.getPong());
		Assert.assertTrue(obj instanceof Pong);
	}
	
	@Test
	public void testClientToServer() {
		
		final Invocation  invocation = new DefaultInvocation(
				"com.github.jremoting.core.test.TestService",
				"1.0", 
				"hello", 
				new Object[]{"xhan"},
				String.class, 
				protocal, 
				serializer.getId());
		invocation.setInvocationId(1);		
		
		Object obj = clientToServer(invocation);
		
		Assert.assertTrue(obj instanceof Invocation);
		
		Invocation decodedInvocation = (Invocation)obj;
		
		Assert.assertEquals("com.github.jremoting.core.test.TestService", decodedInvocation.getServiceName());
		Assert.assertEquals("1.0", decodedInvocation.getServiceVersion());
		Assert.assertEquals("hello", decodedInvocation.getMethodName());
		Assert.assertEquals(1, decodedInvocation.getArgs().length);
		Assert.assertEquals("xhan", decodedInvocation.getArgs()[0]);
		Assert.assertEquals(protocal, decodedInvocation.getProtocal());
		Assert.assertEquals(serializer.getId(), decodedInvocation.getSerializerId());
		Assert.assertEquals(String.class, decodedInvocation.getReturnType());
	}
	
	@Test
	public void testServerToClient() {
		final Invocation invocation = new DefaultInvocation(null, null, null, null,
				String.class, protocal, serializer.getId());
		InvocationResult result = new InvocationResult("hello,world", invocation);
		
		clientCodec = new NettyClientCodec(protocal, new InvocationHolder() {
			@Override
			public Invocation getInvocation(long invocationId) {
				return invocation;
			}
		});
		
		Object obj = serverToClient(result);
		
		Assert.assertTrue(obj instanceof InvocationResult);
		InvocationResult returnResult = (InvocationResult)obj;
		Assert.assertEquals("hello,world",returnResult.getResult());
		
	}
	
	public Object serverToClient(InvocationResult result) {
		EmbeddedChannel clientChannel = new EmbeddedChannel(clientCodec);	
		EmbeddedChannel serverChannel = new EmbeddedChannel(serverCodec);
		
		serverChannel.writeOutbound(result);
		
		Object buffer = serverChannel.readOutbound();
		
		clientChannel.writeInbound(buffer);
		
		Object obj = clientChannel.readInbound();
		return obj;
	}
	
	public Object clientToServer(Invocation invocation) {
		EmbeddedChannel clientChannel = new EmbeddedChannel(clientCodec);	
		EmbeddedChannel serverChannel = new EmbeddedChannel(serverCodec);
		
		clientChannel.writeOutbound(invocation);
		
		Object buffer = clientChannel.readOutbound();

		serverChannel.writeInbound(buffer);
		
	    Object obj = serverChannel.readInbound();
	    
	    return obj;
	}
	
	


}
