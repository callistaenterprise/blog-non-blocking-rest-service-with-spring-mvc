package se.callista.springmvc.asynch.pattern.aggregator;

import com.sun.management.UnixOperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class AggregatorController {

    private static final Logger LOG = LoggerFactory.getLogger(AggregatorController.class);

    private static OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private static final AtomicLong lastRequestId = new AtomicLong(0);
    private static final AtomicLong concurrentRequests = new AtomicLong(0);
    private static long maxConcurrentRequests = 0;

    private RestTemplate      restTemplate       = new RestTemplate();

    @Value("${sp.blocking.url}")
    private String SP_BLOCKING_URL;

    @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    @Value("${statistics.requestsPerLog}")
    private int STAT_REQS_PER_LOG;

    @Autowired
    @Qualifier("dbThreadPoolExecutor")
    private TaskExecutor dbThreadPoolExecutor;

    @RequestMapping("/aggregate-blocking")
    public String blockingAggregator(
        @RequestParam(value = "minMs", required = false, defaultValue = "0") int minMs,
        @RequestParam(value = "maxMs", required = false, defaultValue = "0") int maxMs) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        long reqId = lastRequestId.getAndIncrement();
        long concReqs = concurrentRequests.getAndIncrement();

        updateStatistics(reqId, concReqs);

        LOG.debug("{}: Start blocking routing #{}", concReqs, reqId);

        try {
            ResponseEntity<String> result = restTemplate.getForEntity(
                SP_BLOCKING_URL + "?minMs={minMs}&maxMs={maxMs}", String.class, minMs, maxMs);

            // TODO: Handle status codes other than 200...
            status = result.getStatusCode();

            return result.getBody();

        } finally {
            concurrentRequests.decrementAndGet();
            LOG.debug("{}: Routing of blocking request #{} is done, status: {}", concReqs, reqId, status);
        }
    }

    @RequestMapping("/aggregate-non-blocking")
    public DeferredResult<String> nonBlockingAggregator(
        @RequestParam(value = "dbLookupMs", required = false, defaultValue = "0") int dbLookupMs,
        @RequestParam(value = "dbHits",     required = false, defaultValue = "3") int dbHits,
        @RequestParam(value = "minMs",      required = false, defaultValue = "0") int minMs,
        @RequestParam(value = "maxMs",      required = false, defaultValue = "0") int maxMs) throws IOException {

        long reqId = lastRequestId.getAndIncrement();
        long concReqs = concurrentRequests.getAndIncrement();

        updateStatistics(reqId, concReqs);

        LOG.debug("{}: Start non-blocking aggregation #{}.", concReqs, reqId);

        DeferredResult<String> deferredResult = new DeferredResult<String>();

        dbThreadPoolExecutor.execute(new DbLookupRunnable(dbLookupMs, dbHits, minMs, maxMs, deferredResult));

        LOG.debug("{}: Processing of non-blocking aggregation #{} leave the request thread", concReqs, reqId);

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
}