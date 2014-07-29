package se.callista.springmvc.asynch.pattern.aggregator.nonblocking.lambda;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.common.log.LogHelper;
import se.callista.springmvc.asynch.common.log.LogHelperFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;

@RestController
public class AggregatorNonBlockingLambdaController {

    private static LogHelper LOG;

    @Autowired
    private LogHelperFactory logFactory;

    @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    @Value("${aggregator.timeoutMs}")
    private int TIMEOUT_MS;

    @PostConstruct
    public void initAfterInject() {
        LOG = logFactory.getLog(AggregatorNonBlockingLambdaController.class, "aggregator");
    }

    @Autowired
    @Qualifier("dbThreadPoolExecutor")
    private TaskExecutor dbThreadPoolExecutor;

    /**
     * Sample usage: curl "http://localhost:9080/aggregate-non-blocking-lambda?minMs=1000&maxMs=2000"
     *
     * @param dbLookupMs
     * @param dbHits
     * @param minMs
     * @param maxMs
     * @return
     * @throws java.io.IOException
     */
    @RequestMapping("/aggregate-non-blocking-lambda")
    public DeferredResult<String> nonBlockingAggregator(
        @RequestParam(value = "dbLookupMs", required = false, defaultValue = "0")    int dbLookupMs,
        @RequestParam(value = "dbHits",     required = false, defaultValue = "3")    int dbHits,
        @RequestParam(value = "minMs",      required = false, defaultValue = "0")    int minMs,
        @RequestParam(value = "maxMs",      required = false, defaultValue = "0")    int maxMs) throws IOException {

        // Delegate the whole processing to a executor-instance to avoid concurrency problems with other concurrent requests
        Executor exec = new Executor(LOG, SP_NON_BLOCKING_URL, TIMEOUT_MS, dbThreadPoolExecutor, dbLookupMs, dbHits, minMs, maxMs);

        DeferredResult<String> deferredResult = exec.startNonBlockingExecution();

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }
}