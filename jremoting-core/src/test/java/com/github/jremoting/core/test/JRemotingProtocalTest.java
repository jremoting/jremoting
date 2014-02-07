package com.github.jremoting.core.test;

import junit.framework.Assert;
import io.netty.channel.embedded.EmbeddedChannel;

import org.junit.Test;

import com.github.jremoting.protocal.JRemotingProtocal;
import com.github.jremoting.remoting.NettyMessageCodec;
import com.github.jremoting.serializer.JsonSerializer;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeResult;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.Serializer;
import com.github.jremoting.core.test.TestService.HelloInput;

public class JRemotingProtocalTest {

	private Serializer serializer = new JsonSerializer();
	private JRemotingProtocal protocal = new JRemotingProtocal(new Serializer[]{ serializer});
	
	@Test
	public void testClientToServer() {
		
		final Invoke  invocation = new Invoke(
				"com.github.jremoting.core.test.TestService",
				"1.0", 
				"hello", 
				new Object[]{"xhan"},
				new Class<?>[]{String.class},
				String.class, 
				protocal, 
				serializer);
		invocation.setId(1);		
		
		Object obj = clientToServer(invocation);
		
		Assert.assertTrue(obj instanceof Invoke);
		
		Invoke decodedInvocation = (Invoke)obj;
		
		Assert.assertEquals("com.github.jremoting.core.test.TestService", decodedInvocation.getInterfaceName());
		Assert.assertEquals("1.0", decodedInvocation.getVersion());
		Assert.assertEquals("hello", decodedInvocation.getMethodName());
		Assert.assertEquals(1, decodedInvocation.getArgs().length);
		Assert.assertEquals("xhan", decodedInvocation.getArgs()[0]);
		Assert.assertEquals(protocal, decodedInvocation.getProtocal());
		Assert.assertEquals(serializer.getId(), decodedInvocation.getSerializer());
	}
	
	@Test
	public void testClientToServer2() {
		
		final Invoke  invocation = new Invoke(
				"com.github.jremoting.core.test.TestService",
				"1.0", 
				"hello", 
				new Object[]{new HelloInput(),1,"4"},
				new Class<?>[]{HelloInput.class,Integer.class, String.class},
				String.class, 
				protocal, 
				serializer);
		invocation.setId(1);		
		
		Object obj = clientToServer(invocation);
		
		Assert.assertTrue(obj instanceof Invoke);
		
		Invoke decodedInvocation = (Invoke)obj;
		
		Assert.assertEquals("com.github.jremoting.core.test.TestService", decodedInvocation.getInterfaceName());
		Assert.assertEquals("1.0", decodedInvocation.getVersion());
		Assert.assertEquals("hello", decodedInvocation.getMethodName());
		Assert.assertEquals(3, decodedInvocation.getArgs().length);
		Assert.assertEquals(new HelloInput(), decodedInvocation.getArgs()[0]);
		Assert.assertEquals(1, decodedInvocation.getArgs()[1]);
		Assert.assertEquals("4", decodedInvocation.getArgs()[2]);
		Assert.assertEquals(protocal, decodedInvocation.getProtocal());
		Assert.assertEquals(serializer.getId(), decodedInvocation.getSerializer());
	}
	
	@Test
	public void testServerToClient() {

		InvokeResult result = new InvokeResult("hello,world", 0 ,protocal,serializer);
		


		Object obj = serverToClient(result);
		
		Assert.assertTrue(obj instanceof InvokeResult);
		InvokeResult returnResult = (InvokeResult)obj;
		Assert.assertEquals("hello,world",returnResult.getResult());
		
	}
	
	
	
	public Object serverToClient(InvokeResult result) {
		EmbeddedChannel clientChannel = new EmbeddedChannel(new NettyMessageCodec(protocal));	
		EmbeddedChannel serverChannel = new EmbeddedChannel(new NettyMessageCodec(protocal));
		
		serverChannel.writeOutbound(result);
		
		Object buffer = serverChannel.readOutbound();
		
		clientChannel.writeInbound(buffer);
		
		Object obj = clientChannel.readInbound();
		return obj;
	}
	
	public Object clientToServer(Invoke invocation) {
		EmbeddedChannel clientChannel = new EmbeddedChannel(new NettyMessageCodec(protocal));	
		EmbeddedChannel serverChannel = new EmbeddedChannel(new NettyMessageCodec(protocal));
		
		clientChannel.writeOutbound(invocation);
		
		Object buffer = clientChannel.readOutbound();
	
		serverChannel.writeInbound(buffer);
		
	    Object obj = serverChannel.readInbound();
	    
	    return obj;
	}
	
	


}
