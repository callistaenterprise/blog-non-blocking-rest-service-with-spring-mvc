package se.callista.springmvc.asynch.teststub;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.sun.management.UnixOperatingSystemMXBean;

@RestController
public class ProcessingController {
    
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingController.class);

    private static OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private static final AtomicLong lastRequestId = new AtomicLong(0);
    private static final AtomicLong concurrentRequests = new AtomicLong(0);
    private static long maxConcurrentRequests = 0;

    private Timer timer = new Timer();

    @Value("${statistics.requestsPerLog}")
    private int STAT_REQS_PER_LOG;
    private int defaultMinMs = 0;
    private int defaultMaxMs = 0;

    @RequestMapping("/set-default-processing-time")
    public void setDefaultProcessingTime(
            @RequestParam(value = "minMs", required = true) int minMs,
            @RequestParam(value = "maxMs", required = true) int maxMs) {
        this.defaultMinMs = minMs;
        this.defaultMaxMs = maxMs;
        LOG.info("Set default response time to {} - {} ms.", minMs, maxMs);
    }

    /**
     * Sample usage: curl "http://localhost:9090/process-blocking?minMs=1000&maxMs=2000"
     *
     * @param minMs
     * @param maxMs
     * @return
     */
    @RequestMapping("/process-blocking")
    public ProcessingStatus blockingProcessing(
        @RequestParam(value = "minMs", required = false, defaultValue = "0") int minMs,
        @RequestParam(value = "maxMs", required = false, defaultValue = "0") int maxMs) {

        long reqId = lastRequestId.getAndIncrement();
        long concReqs = concurrentRequests.getAndIncrement();
        
        updateStatistics(reqId, concReqs);

        int processingTimeMs = calculateProcessingTime(minMs, maxMs);

        LOG.debug("{}: Start blocking request #{}, processing time: {} ms.", concReqs, reqId, processingTimeMs);

        try {
            Thread.sleep(processingTimeMs);
        } 
        catch (InterruptedException e) {}
        
        finally {
            concurrentRequests.decrementAndGet();
            LOG.debug("{}: Processing of blocking request #{} is done", concReqs, reqId);        
        }

        return new ProcessingStatus("Ok", processingTimeMs);
    }

    /**
     * Sample usage: curl "http://localhost:9090/process-non-blocking?minMs=1000&maxMs=2000"
     *
     * @param minMs
     * @param maxMs
     * @return
     */
    @RequestMapping("/process-non-blocking")
    public DeferredResult<ProcessingStatus> nonBlockingProcessing(
        @RequestParam(value = "minMs", required = false, defaultValue = "0") int minMs,
        @RequestParam(value = "maxMs", required = false, defaultValue = "0") int maxMs) {

        long reqId = lastRequestId.getAndIncrement();
        long concReqs = concurrentRequests.getAndIncrement();
        
        updateStatistics(reqId, concReqs);
        
        int processingTimeMs = calculateProcessingTime(minMs, maxMs);

        LOG.debug("{}: Start non-blocking request #{}, processing time: {} ms.", concReqs, reqId, processingTimeMs);

        // Create the deferredResult and initiate a callback object, task, with it
        DeferredResult<ProcessingStatus> deferredResult = new DeferredResult<>();
        ProcessingTask task = new ProcessingTask(reqId, concurrentRequests, processingTimeMs, deferredResult);

        // Schedule the task for asynch completion in the future
        timer.schedule(task, processingTimeMs);

        LOG.debug("{}: Processing of non-blocking request #{} leave the request thread", concReqs, reqId);

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }
    
    private void updateStatistics(long reqId, long concReqs) {
        if (concReqs > maxConcurrentRequests) {
            maxConcurrentRequests = concReqs;
        }
        
        if (reqId % STAT_REQS_PER_LOG == 0 && reqId > 0) {
            Object openFiles = "UNKNOWN";
            if (os instanceof UnixOperatingSystemMXBean) {
                openFiles = ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount();
            }
            LOG.info("Statistics: noOfReqs: {}, maxConcReqs: {}, openFiles: {}", reqId, maxConcurrentRequests, openFiles);
        }
    }

    private int calculateProcessingTime(int minMs, int maxMs) {
        if (minMs == 0 && maxMs == 0) {
            minMs = defaultMinMs;
            maxMs = defaultMaxMs;
        }

        if (maxMs < minMs) maxMs = minMs;
        int processingTimeMs = minMs + (int) (Math.random() * (maxMs - minMs));
        return processingTimeMs;
    }
    
}