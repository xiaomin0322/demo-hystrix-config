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
	int getThreadId();
	int getNonThreadedThreadThreadId();
	String withZeroTimeout(String str);
	String exceptionWithFallback(String testStr);

	Throwable exceptionWithFallbackIncludingException(String testStr);

	Future<String> getFuture(String str);
	
	rx.Observable<String> getObservable(String str);
}
