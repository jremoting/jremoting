jremoting是一个类似dubbo的rpc服务治理框架,主要功能包括
  1. 透明方式的rpc调用,支持consumer端异步调用与provider的异步实现
  2. 服务的动态发现
  3. 负载均衡+ failover
  4. 动态路由
  5. 动态分组
  6. 服务限流(开发中)

如何使用：

服务提供方(provider)提供的服务接口定义，并将实现通过spring定义发布到注册中心(registry)

public interface HelloService {
	String hello(String name);
}


public class HelloServiceImpl implements HelloService {

	@Override
	public String hello(String name) {
		return "hello,"+ name;
	}

}

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans.xsd">
	
	<import resource="classpath:jremoting-context.xml"/>
	
	<bean id="helloServiceProvider" class="com.github.jremoting.spring.JRemotingProviderBean" init-method="start">
		<constructor-arg name="interfaceName" value="com.github.jremoting.example.HelloService" />
		<constructor-arg name="version" value="1.0" />
		<constructor-arg name="target" ref="helloService" />
		<constructor-arg name="rpcServer" ref="rpcServer" />
	</bean>
	
	<bean id="helloService" class="com.github.jremoting.example.HelloServiceImpl"></bean>
	
</beans>


public class TestProvider {
	public static void main(String[] args) throws IOException {
		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("server-context.xml");
		
		
		System.in.read();
		
		context.close();
	}
}


服务消费方(consumer)只依赖接口(HelloService)既可以通过rpc的方式调用远程provider的实现

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans.xsd">
	
	<import resource="classpath:jremoting-context.xml"/>
	
	<bean id="helloService" class="com.github.jremoting.spring.JRemotingConsumerBean" init-method="start">
		<constructor-arg name="interfaceName" value="com.github.jremoting.example.HelloService" />
		<constructor-arg name="version" value="1.0" />
		<constructor-arg name="rpcClient" ref="rpcClient" />
	</bean>
</beans>

public class TestClient {
	public static void main(String[] args) throws IOException {
		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("client-context.xml");
		
		HelloService helloService = context.getBean(HelloService.class);
		
		String result = helloService.hello("jremoting");
		
		System.out.println(result);
		
		System.in.read();
		
		context.close();
	}
}

jremoting注册中心基于zookeeper实现的，测试hello world例子需要安装zookeeper. jremoting-context.xml中配置了jremoting运行
定义的bean对象 以及zookeeper连接地址,已经provider端连接的监听端口

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans.xsd">
	
	<!-- 定义serializer,默认提供json与hessian序列化 -->
	<bean id="jsonSerializer" class="com.github.jremoting.serializer.JsonSerializer" ></bean>
	<bean id="hessianSerializer" class="com.github.jremoting.serializer.HessianSerializer" ></bean>


	<!-- 定义服务注册中心,底层采用zookeeper来实现，服务的动态发现与配置的动态推送。除了CacheRegistryWrapper,与ZookeeperRegistry为必选，其他实现分组，路由，权重为可选 -->
	<bean id="registry" class="com.github.jremoting.route.RouteRegistryWrapper">
		<constructor-arg>
			<bean class="com.github.jremoting.group.GroupRegistryWrapper">
				<constructor-arg>
					<bean class="com.github.jremoting.registry.CacheRegistryWrapper">
						<constructor-arg>
							<bean class="com.github.jremoting.registry.ZookeeperRegistry">
								<constructor-arg name="zookeeperConnectionString" value="127.0.0.1:2181" />
							</bean>
						</constructor-arg>
					</bean>
				</constructor-arg>
			</bean>
		</constructor-arg>
	</bean>


	<!-- 定义协议 ，并给协议关联序列化与注册中心-->
	<bean id="jremotingProtocal" class="com.github.jremoting.protocal.JRemotingProtocal">
		<constructor-arg name="registry" ref="registry" />
		<constructor-arg name="serializers">
			<array>
				<ref bean="jsonSerializer"/>
				<ref bean="hessianSerializer"/>
			</array>
		</constructor-arg>
	</bean>

	<!-- 定义io线程池 底层netty处理io事件与ChannelHandler的线程池 -->
	<bean id="eventExecutor" class="com.github.jremoting.transport.EventExecutor"></bean>
	
	<!-- 定义用户线程池，可用于consumer端的异步回调处理，或者provider端的服务方法处理-->
	<bean id="executor" class="com.github.jremoting.util.concurrent.Executors" factory-method="newExecutor">
		<constructor-arg name="corePoolSize" value="3"></constructor-arg>
		<constructor-arg name="maxPoolSize" value="20"></constructor-arg>
		<constructor-arg name="queueSize" value="20"></constructor-arg>
	</bean>

	<!-- 定义rpc client对象，用户定制consumer端需要的各种组件，包括协议，默认序列化方式，异步调用线程池，调用拦截器 -->
	<bean id="rpcClient" class="com.github.jremoting.transport.DefaultRpcClient">
		<constructor-arg name="protocal" ref="jremotingProtocal" />
		<constructor-arg name="defaultSerializer" ref="hessianSerializer" />
		<constructor-arg name="eventExecutor" ref="eventExecutor" />
		<constructor-arg name="asyncInvokeExecutor" ref="executor" />
		<constructor-arg name="invokeFilters">
			<list>
				<!-- 实现重试功能的拦截器 -->
				<bean class="com.github.jremoting.invoke.RetryInvokeFilter" />
				<!-- 实现软负载与failover的拦截器 , 负载方式为在可用provider间随机调用-->
				<bean class="com.github.jremoting.invoke.ClusterInvokeFilter" />
			</list>
		</constructor-arg>
	</bean>

	<!-- 定义rpc server对象，定制server端的各种组件，包括协议，执行provider方法的线程池，调用拦截器 -->
	<bean id="rpcServer" class="com.github.jremoting.transport.DefaultRpcServer">
		<constructor-arg name="eventExecutor" ref="eventExecutor" />
		<constructor-arg name="serviceExecutor" ref="executor" />
		<constructor-arg name="protocal" ref="jremotingProtocal" />
		<constructor-arg name="port" value="8687" />
		<constructor-arg name="invokeFilters">
			<list></list>
		</constructor-arg>
	</bean>
	
	<!-- 监听spring容器的关闭事件，同时关闭jremoting需要关闭的资源，包括监听,注册中心，线程池 -->
	<bean id="jremmotingLifeCycle" class="com.github.jremoting.spring.JRemotingLifeCycleBean">
		<property name="rpcClients">
			<list>
				<ref bean="rpcClient"/>
			</list>
		</property>
		<property name="rpcServers">
			<list>
				<ref bean="rpcServer"/>
			</list>
		</property>
	</bean>
	
</beans>


下面是异步与泛型调用例子
	//异步调用
		AsyncHelloService asyncHelloService = (AsyncHelloService)helloService;
		
		ListenableFuture<String> future = asyncHelloService.$hello("jremoting async invoke!");
		
		System.out.println(future.get());
		
		
		//异步listener方式调用，注意operationComplete是在jremoting-context.xml中配置的专门executor上执行的。也可以自己指定executor
		future = asyncHelloService.$hello("jremoting async use future listener!");
		
		future.addListener(new FutureListener<String>() {
			
			@Override
			public void operationComplete(ListenableFuture<String> future) {
				if(future.isSuccess()) {
					System.out.println(future.result());
				}
			}
		});
		
		
		//如果consumer端，不想依赖provider定义的接口，也可以直接调用远程方法，不过要把复杂对象都用map来代替，返回结果也一样
		RpcClient rpcClient = context.getBean(RpcClient.class);
		ServiceConsumer consumer = new ServiceConsumer("com.github.jremoting.example.HelloService", "1.0", rpcClient).start();
		
		Object obj = consumer.invoke("hello", new String[]{java.lang.String.class.getName()}, new Object[]{"generic invoke!"});
		
		System.out.println(obj);
