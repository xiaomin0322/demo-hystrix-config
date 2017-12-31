package org.tiger.demohystrixconfig.test;

import java.util.concurrent.Future;

public interface Service {
	 String get(String str);
	 String get2(String str);
	 String throwException() throws MyException;
	 String withTimeout(String str);
	/**
	 * execution.isolation.thread.interruptOnTimeout
	 * true
	 * @param string
	 * @return
	 */
	String interruptOnTimeout(String  string);

	/**
	 * execution.isolation.thread.interruptOnTimeout
	 * false
	 * @param string
	 * @return
	 */
	String interruptOnTimeoutFalse(String string);
	/**
	 * execution.isolation.semaphore.maxConcurrentRequests
	 */
	String maxConcurrentRequests(String string );

	/**
	 * fallback.isolation.semaphore.maxConcurrentRequests
	 * @param string
	 * @return
	 */
	String fallbackMaxConcurrentRequests(String string);
	/**
	 * hystrix.command.default.fallback.enabled
	 * 当执行失败或者请求被拒绝，是否会尝试调用hystrixCommand.getFallback() 。默认true
	 * @param string
	 * @return
	 */
	String enabled(String string);

	/**
	 * hystrix.command.default.metrics.rollingStats.timeInMilliseconds
	 * 设置统计的时间窗口值的，毫秒值，circuit break 的打开会根据1个rolling window的统计来计算。若rolling window被设为10000毫秒，
	 * 则rolling window会被分成n个buckets，每个bucket包含success，
	 * failure，timeout，rejection的次数的统计信息。默认10000
	 * @param string
	 * @return
	 */
	String timeInMilliseconds(String string);

	/**
	 * 用来跟踪circuit的健康性，如果未达标则让request短路。默认true
	 * @param string
	 * @return
	 */
	String circuitBreakerEnable(String string);

	/**
	 * 一个rolling window内最小的请求数。如果设为20，那么当一个rolling
	 * window的时间内（比如说1个rolling window是10秒）收到19个请求，
	 * 即使19个请求都失败，也不会触发circuit break。默认20
	 * @param string
	 * @return
	 */
	String requestVolumeThreshold(String string);

	/**
	 * 触发短路的时间值，默认5000
	 * @param string
	 * @return
	 */
	String sleepWindowInMilliseconds(String string);

	/**
	 * 强制打开熔断器，如果打开这个开关，那么拒绝所有request，默认false
	 * @param string
	 * @return
	 */
	String forceOpen(String string);

	/**
	 * 强制关闭熔断器 如果这个开关打开，circuit将一直关闭且忽略
	 * @param string
	 * @return
	 */
	String forceClosed(String string);

	/**
	  设置一个rolling window被划分的数量，若numBuckets＝10，*
	 * @param string
	 * @return
	 */
	String numBuckets(String string);

	/**
	 * 执行时是否enable指标的计算和跟踪，默认true
	 * @param string
	 * @return
	 */
	String rollingPercentileEabled(String string);

	/**
	 *设置rolling percentile window的时间，
	 * @return
	 */
	String rollingPercentileTimeInMilliseconds(String string);

	/**
	 * 设置rolling percentile window的numberBuckets。
	 * @param string
	 * @return
	 */
	String rollingPercentileNumBuckets(String string);

	/**
	 * 如果bucket size＝100，window＝10s，
	 * 若这10s里有500次执行，只有最后100次执行会被统计到bucket里去。
	 * 增加该值会增加内存开销以及排序的开销。默认100
	 * @return
	 */
	String bucketSize(String string);

	String intervalInMilliseconds(String string);
	int getThreadId();
	int getNonThreadedThreadThreadId();
	String withZeroTimeout(String str);
	String exceptionWithFallback(String testStr);

	Throwable exceptionWithFallbackIncludingException(String testStr);

	Future<String> getFuture(String str);
	
	rx.Observable<String> getObservable(String str);
}
