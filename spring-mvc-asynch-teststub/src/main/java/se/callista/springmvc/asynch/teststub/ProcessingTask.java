package se.callista.springmvc.asynch.teststub;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

public class ProcessingTask extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessingTask.class);

    private long reqId;
    private AtomicLong concurrentRequests;
	private DeferredResult<ProcessingStatus> deferredResult;
	private int processingTimeMs;
	
	public ProcessingTask(long reqId, AtomicLong concurrentRequests, int processingTimeMs, DeferredResult<ProcessingStatus> deferredResult) {
	    this.reqId = reqId;
	    this.concurrentRequests = concurrentRequests;
	    this.processingTimeMs = processingTimeMs;
		this.deferredResult = deferredResult;
	}
	
	@Override
	public void run() {
        
	    long concReqs = concurrentRequests.getAndDecrement();

        if (deferredResult.isSetOrExpired()) {
            LOG.warn("{}: Processing of non-blocking request #{} already expired", concReqs, reqId);        
	    } else {
	        boolean deferredStatus = deferredResult.setResult(new ProcessingStatus("Ok", processingTimeMs));
            LOG.debug("{}: Processing of non-blocking request #{} done, deferredStatus = {}", concReqs, reqId, deferredStatus);        
	    }
	}
}