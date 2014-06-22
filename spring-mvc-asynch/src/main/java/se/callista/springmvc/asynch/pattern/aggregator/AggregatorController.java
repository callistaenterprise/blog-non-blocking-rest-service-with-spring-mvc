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
import se.callista.springmvc.asynch.common.log.LogHelper;
import se.callista.springmvc.asynch.common.log.LogHelperFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class AggregatorController {

    private static LogHelper LOG;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private LogHelperFactory logFactory;

    @Value("${sp.blocking.url}")
    private String SP_BLOCKING_URL;

    @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    @PostConstruct
    public void initAfterInject() {
        LOG = logFactory.getLog(AggregatorController.class, "aggregator");
    }

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

        LOG.logStartBlocking();

        String aggregatedResult = "";
        try {

            for (int i = 0; i < 3; i++) {
                LOG.logStartProcessingStepBlocking(i);
                ResponseEntity<String> result = restTemplate.getForEntity(
                        SP_BLOCKING_URL + "?minMs={minMs}&maxMs={maxMs}", String.class, minMs, maxMs);

                // TODO: Handle status codes other than 200...
                status = result.getStatusCode();
                LOG.logEndProcessingStepBlocking(i, status.value());

                aggregatedResult += result.getBody() + '\n';
            }

            return aggregatedResult;

        } finally {
            LOG.logEndBlocking(status.value());
        }
    }

    @RequestMapping("/aggregate-non-blocking")
    public DeferredResult<String> nonBlockingAggregator(
        @RequestParam(value = "dbLookupMs", required = false, defaultValue = "0") int dbLookupMs,
        @RequestParam(value = "dbHits",     required = false, defaultValue = "3") int dbHits,
        @RequestParam(value = "minMs",      required = false, defaultValue = "0") int minMs,
        @RequestParam(value = "maxMs",      required = false, defaultValue = "0") int maxMs) throws IOException {

        LOG.logStartNonBlocking();

        DeferredResult<String> deferredResult = new DeferredResult<String>();

        dbThreadPoolExecutor.execute(new DbLookupRunnable(LOG, dbLookupMs, dbHits, SP_NON_BLOCKING_URL, minMs, maxMs, deferredResult));

        LOG.logLeaveThreadNonBlocking();

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }
}