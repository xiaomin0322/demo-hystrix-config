package org.tiger.demohystrixconfig.test;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.tiger.demohystrixconfig.ZookeeperConfig;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;

import util.ZkUtils;
import zk.HystrixZKClient;

@Component
public class HystrixApplicationListener implements
		ApplicationListener<ContextRefreshedEvent> {

		Logger logger = LoggerFactory.getLogger(HystrixApplicationListener.class);
		private static ConcurrentHashMap<Class<?>, String> defaultNameCache = new ConcurrentHashMap<Class<?>, String>();

	  /* package */static String getDefaultNameFromClass(Class<?> cls) {
	        String fromCache = defaultNameCache.get(cls);
	        if (fromCache != null) {
	            return fromCache;
	        }
	        // generate the default
	        // default HystrixCommandKey to use if the method is not overridden
	        String name = cls.getSimpleName();
	        if (name.equals("")) {
	            // we don't have a SimpleName (anonymous inner class) so use the full class name
	            name = cls.getName();
	            name = name.substring(name.lastIndexOf('.') + 1, name.length());
	        }
	        defaultNameCache.put(cls, name);
	        return name;
	  	}
	
		private static HystrixCommandKey initCommandKey(final HystrixCommandKey fromConstructor, Class<?> clazz) {
	        if (fromConstructor == null || fromConstructor.name().trim().equals("")) {
	            final String keyName = getDefaultNameFromClass(clazz);
	            return HystrixCommandKey.Factory.asKey(keyName);
	        } else {
	            return fromConstructor;
	        }
	    }

		private static HystrixCommandProperties initCommandProperties(HystrixCommandKey commandKey, HystrixPropertiesStrategy propertiesStrategy, HystrixCommandProperties.Setter commandPropertiesDefaults) {
	        if (propertiesStrategy == null) {
	            return HystrixPropertiesFactory.getCommandProperties(commandKey, commandPropertiesDefaults);
	        } else {
	            // used for unit testing
	            return propertiesStrategy.getCommandProperties(commandKey, commandPropertiesDefaults);
	        }
	    }
	    
	    
		/**
		 * 表示所属的group，threadKey 一个group共用线程池 默认类名(简称) HystrixCommandServiceImpl，线程池用的是类名，(最好自定义)group，threadKey以免有冲突
		 * 类名最好是，全包名+类名，这样就不会有冲突，可以在动态配置archaius包一层，将每个类的配置做一个文件夹存放早zk，方便管理，不会冲突
		 * @param
		 * @param cb
		 * @return
		 */

		private String getHystrixGroupKey(Class<?> objClass,
				com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand cb) {
			String name = cb.groupKey().length() == 0 ? cb.commandKey() : cb.groupKey();
			//return name.length() == 0 ? joinPoint.getSignature().toShortString() : name;
			return name.length() == 0 ? objClass.getSimpleName(): name;
		}
		
		/**
		 * 默认值：当前执行方法名简称 如:get,信号量是以方法名简称为单位。。(最好自定义)commandKey以免有冲突
		 * 方法名称最好是，全包名+类名+方法名称，这样就不会有冲突，可以在动态配置archaius包一层，将每个类的配置做一个文件夹存放早zk，方便管理，不会冲突
		 * @param
		 * @param cb
		 * @return
		 */
		private String getHystrixcommandKey(Method method,
				com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand cb) {
			//Method method = getMethod(joinPoint);
			//String name =method.toGenericString();
			String name =method.getName();
			return name;
		}

		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			// logger.debug("------初始化执行----");
			System.out.println("------初始化执行----");
			try {
				// 获取上下文
				ApplicationContext context = event.getApplicationContext();
				// 获取所有beanNames
				String[] beanNames = context.getBeanNamesForType(Object.class);
				for (String beanName : beanNames) {
					Class<?> objClass = AopTargetUtils.getTarget(
							context.getBean(beanName)).getClass();
					Method[] methods = objClass.getDeclaredMethods();
					List<HystrixCommandVo> hystrixCommandVos = new ArrayList<HystrixCommandVo>();

					for (Method method : methods) {
						HystrixCommand hystrixCommand = method
								.getAnnotation(HystrixCommand.class);
						if (hystrixCommand == null) {
							continue;
						}
						// do something
							HystrixCommandVo commandVo = new HystrixCommandVo(
								hystrixCommand);
						commandVo.setMethodName(method.getName());
						commandVo.setClassName(objClass.getSimpleName());
						commandVo.setPackageName(objClass.getPackage().getName());
						commandVo.setProjectName(Class.class.getClass()
								.getResource("/").getPath());
						commandVo.setServiceIp(InetAddress.getLocalHost()
								.getHostAddress());
						commandVo.setClassNameAll(objClass.getName());
						hystrixCommandVos.add(commandVo);


						String  commandKey =  getHystrixcommandKey(method, hystrixCommand);
						if(StringUtils.isBlank(commandVo.getCommandKey())){
							commandVo.setCommandKey(commandKey);
						}

						String  gropyKey =  getHystrixGroupKey(objClass, hystrixCommand);
						if(StringUtils.isBlank(commandVo.getGroupKey())){
							commandVo.setGroupKey(gropyKey);
						}

						String  threadKey =  getHystrixGroupKey(objClass, hystrixCommand);
						if(StringUtils.isBlank(commandVo.getThreadPoolKey())){
							commandVo.setThreadPoolKey(threadKey);
						}
					/*	System.out.println("commandKey:"+commandKey+" gropyKey:"+gropyKey+" threadKey:"+threadKey);
						System.out.println("注解方法：" + method.getName() + ",===="+ commandVo);
*/
					/*	List<HystrixPropertyVo> commadnProperties
								= commandVo.getCommandProperties();
						if(!ZkUtils.configZk(commadnProperties,commandVo)){
							logger.info("error:  properties  execution.isolation.thread.timeoutInMilliseconds");
							throw new RuntimeException("error: execution.isolation.thread.timeoutInMilliseconds");
						}*/


					/*	if("maxConcurrentRequests".equalsIgnoreCase(method.getName())) {
							List<HystrixPropertyVo> commadnProperties
									= commandVo.getCommandProperties();
							if (!ZkUtils.configZk(commadnProperties, commandVo)) {
								logger.info("error:  properties  execution.isolation.thread.interruptOnTimeoutFalse");
								throw new RuntimeException("error: execution.isolation.thread.interruptOnTimeoutFalse");
							}
						}
					*/
						// /hystrix/org.springframework.integration.hystrix.HystrixCommandServiceImpl
						String classNodeNamePath = ZookeeperConfig.zkConfigRootPath + "/"
								+ commandVo.getClassNameAll();
						// /hystrix/org.springframework.integration.hystrix.hystrixCommandServiceImpl_method_get_ip
						final String methodsNodeNamePath = classNodeNamePath
								+ "/method_" + commandVo.getMethodName() + "_"
								+ commandVo.getServiceIp();

						//System.out.println(methodsNodeNamePath);

					}
				}
				} catch (Exception e) {
					e.printStackTrace();
			}
	}
}