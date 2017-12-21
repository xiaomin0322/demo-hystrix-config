package org.tiger.demohystrixconfig.test;

import java.util.concurrent.Future;

import rx.Observable;
import rx.Subscriber;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;

@org.springframework.stereotype.Service("hystrixCommandServiceImpl")
public class HystrixCommandServiceImpl implements  Service{

	public static final int TEST_TIMEOUT = 300;

	@Override
	@HystrixCommand(
			commandProperties={ @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE"),
					@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000"),
					@HystrixProperty(name = "execution.isolation.semaphore.maxConcurrentRequests", value = "2")
					}
			)
	public String get(String str) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("str====="+str);
		return str;
	}
	
	@Override
	@HystrixCommand(
			commandProperties={  @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD") },
			
			threadPoolProperties = { @HystrixProperty(name = "coreSize", value = "5"),
			@HystrixProperty(name = "maxQueueSize", value = "20") })
	public String get2(String str) {
		return str;
	}

	@Override
	@HystrixCommand
	public String throwException() throws MyException {
		throw new MyException();
	}

	/**
	 * execution.isolation.thread.timeoutInMilliseconds
	 * 命令执行超时时间，默认1000ms
	 * @param str
	 * @return
	 */
	@Override
	@HystrixCommand(commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = TEST_TIMEOUT + "") })
	public String withTimeout(String str) {
		try {
			//Thread.sleep(2 * TEST_TIMEOUT);
			System.out.println("==========="+str);
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * execution.isolation.thread.interruptOnTimeout
	 * 执行是否启用超时，默认启用true
	 * @param
	 * @return
	 */

	@HystrixCommand(commandProperties = {
		@HystrixProperty(name="execution.isolation.thread.interruptOnTimeout",value="false")
	})
	public String interruptOnTimeout(String  string) {
		try{
			System.err.println("String :" + string);
			Thread.sleep(2000);
		}catch (InterruptedException e){
			e.printStackTrace();
		}
		return string;
	}

	/**
	 * execution.isolation.thread.interruptOnTimeout
	 * 不启用超时
	 * false
	 * @return
	 */
	@HystrixCommand(commandProperties = {
			@HystrixProperty(name="execution.isolation.thread.interruptOnTimeout",value = "false")
	})
	public String interruptOnTimeoutFalse(String string){
		try{
			System.err.println("String  :" +string);
			Thread.sleep(2000);
		}catch (InterruptedException e){
			e.printStackTrace();
		}
		return string;
	}

	/**
	 *
	 * @param string
	 * @return
	 */
	@Override
	@HystrixCommand(fallbackMethod = "fallbackMethod",
			commandProperties =
			{ @HystrixProperty(name = "execution.isolation.semaphore.maxConcurrentRequests",
					value = "50") })
	public String maxConcurrentRequests(String string) {
		return string;
	}

	/**
	 * 如果并发数达到该设置值，请求会被拒绝和抛出异常并且fallback不会被调用。默认10
	 * @param string
	 * @return
	 */
	@Override
	@HystrixCommand(fallbackMethod = "fallbackMethod",
		commandProperties = {
			@HystrixProperty(name = "fallback.isolation.semaphore.maxConcurrentRequests",
			value="10")
		})
	public String fallbackMaxConcurrentRequests(String string) {
		return string;
	}

	/**
	 * 当执行失败或者请求被拒绝，是否会尝试调用hystrixCommand.getFallback() 。默认true
	 * @param string
	 * @return
	 */
	@Override
	@HystrixCommand(commandProperties =
			{@HystrixProperty(name = "fallback.enabled ",value="true")})
	public String enabled(String string) {
		return string;
	}
	/**
	 * execution.isolation.thread.timeoutInMilliseconds
	 * @param str
	 * @return
	 */
	@HystrixCommand(commandProperties = { @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "0") })
	public String withZeroTimeout(String str) {
		try {
			Thread.sleep(2 * TEST_TIMEOUT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * execution.isolation.strategy
	 * 隔离策略，默认是Thread, 可选Thread｜Semaphore
	 *
	 * @return
	 */
	@Override
	// executionIsolationStrategy
	@HystrixCommand(commandProperties = { @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD") })
	public int getThreadId() {
		return Thread.currentThread().hashCode();
	}

	/**
	 *
	 * 隔离策略，默认是Thread, 可选Thread｜Semaphore
	 * @return
	 */
	@Override
	@HystrixCommand(commandProperties = { @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE") })
	public int getNonThreadedThreadThreadId() {
		return Thread.currentThread().hashCode();
	}

	@Override
	//@HystrixCommand(/*fallbackMethod = "fallback"*//*,ignoreExceptions={MyRuntimeException.class}*/)
	@HystrixCommand(
			fallbackMethod = "fallback",
			commandProperties={
			@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "4"),
			@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "5000"),
			@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50")})
	public String exceptionWithFallback(String s) {
		System.out.println("exceptionWithFallback>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		throw new MyRuntimeException();
	}

	public String fallback(String s) {
		return s;
	}

	@Override
	@HystrixCommand(fallbackMethod = "fallbackWithException"/*,ignoreExceptions={MyRuntimeException.class}*/)
	public Throwable exceptionWithFallbackIncludingException(String testStr) {
		throw new MyRuntimeException();
	}

	public Throwable fallbackWithException(String testStr, Throwable t) {
		return t;
	}

	@Override
	@HystrixCommand
	public Future<String> getFuture(final String str) {
		return new AsyncResult<String>() {
			@Override
			public String invoke() {
				return str;
			}
		};
	}

	@Override
	@HystrixCommand
	public Observable<String> getObservable(final String str) {
		 return Observable.create(new Observable.OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> observer) {
				 try {
                     if (!observer.isUnsubscribed()) {
                         observer.onNext(str);
                         observer.onCompleted();
                     }		
                 } catch (Exception e) {
                     observer.onError(e);
                 }
				
			}
         }); 
	}
}
