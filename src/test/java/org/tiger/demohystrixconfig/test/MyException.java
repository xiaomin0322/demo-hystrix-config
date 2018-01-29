package org.tiger.demohystrixconfig.test;

import com.netflix.hystrix.strategy.config.Configer;

public class MyException extends Exception {
	
	public String config = Configer.val;

}
