package org.tiger.demohystrixconfig.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tiger.Application;

import com.netflix.config.DynamicPropertyFactory;

import rx.Observer;
import rx.functions.Action1;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:spring/applicationConfig.xml" })
public class HystrixCommandTest {

	private static final String TEST_STR = "TEST_STR";
	@Autowired
	@Qualifier(value = "hystrixCommandServiceImpl")
	private Service service;

	
	@Test
	public void getFuture()throws Exception {
		System.out.println(service.getFuture(TEST_STR).get());
	}
	
	@Test
	public void getObservable() {
		
		 // blocking
	    assertEquals(TEST_STR, service.getObservable(TEST_STR).toBlocking().single());
		
	 // non-blocking 
	    // - this is a verbose anonymous inner-class approach and doesn't do assertions
		service.getObservable(TEST_STR).subscribe(new Observer() {

			@Override
			public void onCompleted() {
				// TODO Auto-generated method stub
				System.out.println("onCompleted");
			}

			@Override
			public void onError(Throwable e) {
				e.printStackTrace();
			}

			@Override
			public void onNext(Object t) {
				System.out.println("onNext: " + t);
			}
			
		});
		
		// non-blocking
	    // - also verbose anonymous inner-class
	    // - ignore errors and onCompleted signal
		
		service.getObservable(TEST_STR).subscribe(new Action1<String>() {
		        @Override
		        public void call(String v) {
		            System.out.println("Action1 onNext: " + v);
		        }

		    });
	}
	
	
	
	@Test
	public void testHystrix() {
		assertEquals(TEST_STR, service.get(TEST_STR));
	}
	


	@Test
	public void testTimeoutHystrix() {
		long start = System.currentTimeMillis();
		try {
			service.withTimeout(TEST_STR);
		} catch (Exception e) {
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();
		//assertTrue(end - start > HystrixCommandServiceImpl.TEST_TIMEOUT);
		//assertTrue(end - start < HystrixCommandServiceImpl.TEST_TIMEOUT * 1.1f);
	}
	
	
	@Test
	public void testTimeoutHystrixAll()throws Exception {
		Application.startZookeeper();
		
		
		for(;;){
			try {
				service.withTimeout(TEST_STR);
			} catch (Exception e) {
				e.printStackTrace();
			}
			 String nodeProperty = DynamicPropertyFactory.getInstance()
		              .getStringProperty("config", "<none>")
		              .get();
		      // before this set hystrix.command.HystrixCommandKey.execution.isolation.thread.timeoutInMilliseconds filed
		      // ExampleKey is HystrixCommandKey singleton
		      String dynamicProperty = DynamicPropertyFactory.getInstance()
		              .getStringProperty("hystrix.command.ExampleKey.execution.isolation.thread.timeoutInMilliseconds",
		                      "<none>")
		              .get();

		      System.out.println(" config node property:{},dynamicProperty:{}" +nodeProperty+"    "+dynamicProperty);
			Thread.sleep(5000);
		}
		

		//assertTrue(end - start > HystrixCommandServiceImpl.TEST_TIMEOUT);
		//assertTrue(end - start < HystrixCommandServiceImpl.TEST_TIMEOUT * 1.1f);
	}
	
	

	@Test
	public void testZeroTimeoutHystrix() {
		long start = System.currentTimeMillis();
		service.withZeroTimeout(TEST_STR);

		long end = System.currentTimeMillis();
		assertTrue(end - start > HystrixCommandServiceImpl.TEST_TIMEOUT * 2);
		assertTrue(end - start < HystrixCommandServiceImpl.TEST_TIMEOUT * 2.1f);
	}

	//@Test(expected = MyException.class)
	@Test(expected = com.netflix.hystrix.exception.HystrixRuntimeException.class)
	public void testException() throws MyException {
		service.throwException();
	}

	@Test
	public void testThreaded() throws MyException {
		int threadId = Thread.currentThread().hashCode();
		int serviceThreadId = service.getThreadId();

		assertNotEquals(threadId, serviceThreadId);
	}

	@Test
	public void testNonThreaded() throws MyException {
		int threadId = Thread.currentThread().hashCode();
		int serviceThreadId = service.getNonThreadedThreadThreadId();

		assertEquals(threadId, serviceThreadId);
	}

	@Test
	public void testExceptionWithFallback() throws MyException {
		assertEquals(TEST_STR, service.exceptionWithFallback(TEST_STR));
	}

	@Test
	public void testExceptionPassingExceptionToFallback() throws MyException {
		Throwable t = service.exceptionWithFallbackIncludingException(TEST_STR);
		assertTrue(t instanceof MyRuntimeException);
	}

	

	@Test
	public void testerrorThresholdPercentage() throws Exception {
		for (int i = 0; i < 100; i++) {
			try{
				
				System.out.println("index" + i + " : " + service.exceptionWithFallback(TEST_STR));
			}catch (MyRuntimeException e) {
				System.out.println("MyRuntimeException=================="+i+" "+e.getMessage());
			}catch (Exception e) {
				System.out.println("Exception=================="+i+"   "+e.getMessage());
			}
			
			Thread.sleep(1000);
			
		}
	}

}
