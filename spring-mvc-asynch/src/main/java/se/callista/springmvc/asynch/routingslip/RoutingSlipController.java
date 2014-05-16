package se.callista.springmvc.asynch.routingslip;

import com.sun.management.UnixOperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.routingslip.statemachine.State;
import se.callista.springmvc.asynch.routingslip.statemachine.StateMachine;
import se.callista.springmvc.asynch.routingslip.util.DeferredResultWithBlockingWait;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class RoutingSlipController {
    
    private static final Logger LOG = LoggerFactory.getLogger(RoutingSlipController.class);

    private static OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private static final AtomicLong lastRequestId = new AtomicLong(0);
    private static final AtomicLong concurrentRequests = new AtomicLong(0);
    private static long maxConcurrentRequests = 0;


    @Autowired
    private StateMachine stateMachine;

    @Value("${statistics.requestsPerLog}")
    private int STAT_REQS_PER_LOG;

    /**
     * curl "http://localhost:9080/routing-slip-non-blocking"
     *
     * @return
     */
    @RequestMapping("/routing-slip-non-blocking")
    public DeferredResult<String> nonBlockingRoutingSlip() throws IOException {

        long reqId = lastRequestId.getAndIncrement();
        long concReqs = concurrentRequests.getAndIncrement();

        updateStatistics(reqId, concReqs);

        LOG.debug("{}: Start non-blocking routing slip #{}.", concReqs, reqId);

        // Create a deferred result
        DeferredResult<String> deferredResult = new DeferredResult<>();

        // Kick off the asynch processing of a number of sequentially executed asynch processing steps
        stateMachine.initProcessing(State.ProcessType.ASYNCH_PROCESS, reqId, deferredResult, concurrentRequests);

        LOG.debug("{}: Processing of non-blocking routing slip #{} leave the request thread", concReqs, reqId);

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }

    /**
     * curl "http://localhost:9080/routing-slip-blocking"
     *
     * @return
     */
    @RequestMapping("/routing-slip-blocking")
    public String blockingRoutingSlip() {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        long reqId = lastRequestId.getAndIncrement();
        long concReqs = concurrentRequests.getAndIncrement();
        
        updateStatistics(reqId, concReqs);
        
        LOG.debug("{}: Start blocking routing slip #{}", concReqs, reqId);

        try {
            // Create a deferred result that we can wait for in blocking mode
            DeferredResultWithBlockingWait<String> deferredResult = new DeferredResultWithBlockingWait<>();

            // Kick off the processing
            stateMachine.initProcessing(State.ProcessType.SYNCH_PROCESS, reqId, deferredResult, concurrentRequests);

            // Wait (blocking) for its completion
            deferredResult.await();

            // Return the result
            return deferredResult.getResult().toString();

        } finally {
            concurrentRequests.decrementAndGet();
            LOG.debug("{}: Routing slip of blocking request #{} is done, status: {}", concReqs, reqId, status);
        }
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
}