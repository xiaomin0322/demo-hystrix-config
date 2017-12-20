package util;

import org.apache.commons.collections.CollectionUtils;
import org.tiger.demohystrixconfig.ZookeeperConfig;
import org.tiger.demohystrixconfig.test.HystrixCommandVo;
import org.tiger.demohystrixconfig.test.HystrixPropertyVo;
import zk.HystrixZKClient;

import java.util.List;

/**
 * User: Clement
 * PACKAGE_NAME: util
 * MONTH_NAME_SHORT: 十二月
 * DAY_NAME_SHORT: 星期三
 * PROJECT_NAME: demo-hystrix-confg
 * TIME: 21:39
 */
public class ZkUtils {

    /**
     * 通用公共类
     * @param propertyVoList
     * @param commandVo
     * @return
     */
    public static boolean configZk(List<HystrixPropertyVo> propertyVoList,HystrixCommandVo commandVo){
        String commandZkName = "hystrix.command.";
        String commandKeyZkName = commandZkName + commandVo.getCommandKey();
        if (CollectionUtils.isEmpty(propertyVoList)){
            return  false;
        }
        try {
            for(HystrixPropertyVo p: propertyVoList){
                String name = p.getName();
                String value = p.getValue();
                String commandZkStoreKey=commandKeyZkName+"."+name;
                commandZkStoreKey  = ZookeeperConfig.zkConfigRootPath + "/"
                        + commandZkStoreKey;
                System.out.println(commandZkStoreKey);
                HystrixZKClient.appendEphemeralNode(commandZkStoreKey,value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
