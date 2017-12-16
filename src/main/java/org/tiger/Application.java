package org.tiger;

import static org.tiger.demohystrixconfig.ZookeeperConfig.zkConfigRootPath;
import static org.tiger.demohystrixconfig.ZookeeperConfig.zkConnectionString;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.tiger.demohystrixconfig.ZooKeeperPathCacheConfigurationSource;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicWatchedConfiguration;

//@SpringBootApplication
/**
 * // 创建节点，数据存储在节点上，nodecache
create path /myapp/config "hystrix.command.ExampleKey.execution.isolation.thread.timeoutInMilliseconds=4000"

// 创建节点，pathcache
create path /myapp/config/hystrix.command.ExampleKey.execution.isolation.thread.timeoutInMilliseconds 4000

 * @author zzm
 *
 */
public class Application {


	public static void main(String[] args) {
	//	SpringApplication.run(Application.class, args);
		startZookeeper();
	}

	public static void startZookeeper() {
		CuratorFramework client = CuratorFrameworkFactory.newClient(zkConnectionString,
				new ExponentialBackoffRetry(1000, 3));
		if(!client.getState().equals(CuratorFrameworkState.STARTED)){
			client.start();
		}

		//ZookeeperNodeCacheConfigurationSource zkConfigSource = new ZookeeperNodeCacheConfigurationSource(client, zkConfigRootPath);
		//change cache here
	ZooKeeperPathCacheConfigurationSource zkConfigSource = new ZooKeeperPathCacheConfigurationSource(client, zkConfigRootPath);
		try {
			zkConfigSource.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		DynamicWatchedConfiguration zkDynamicConfig = new DynamicWatchedConfiguration(zkConfigSource);
		ConfigurationManager.install(zkDynamicConfig);
	}

}
