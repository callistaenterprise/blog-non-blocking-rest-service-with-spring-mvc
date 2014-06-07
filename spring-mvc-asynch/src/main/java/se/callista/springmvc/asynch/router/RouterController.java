package se.callista.springmvc.asynch.router;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import com.ning.http.client.AsyncHttpClient;
import com.sun.management.UnixOperatingSystemMXBean;
import se.callista.springmvc.asynch.util.LogHelper;

@RestController
public class RouterController {

    private final LogHelper LOG;

    private RestTemplate      restTemplate       = new RestTemplate();
    private AsyncRestTemplate asyncRestTemplate  = new AsyncRestTemplate();
    private AsyncHttpClient   asyncHttpClient    = new AsyncHttpClient();
    
    @Value("${sp.blocking.url}")
    private String SP_BLOCKING_URL;

    @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    @Value("${statistics.requestsPerLog}")
    private int STAT_REQS_PER_LOG;

    private RouterController() {
        LOG = new LogHelper(RouterController.class, "router", STAT_REQS_PER_LOG);
    }

    /**
     * Sample usage: curl "http://localhost:9080/route-blocking?minMs=1000&maxMs=2000"
     *
     * @param minMs
     * @param maxMs
     * @return
     */
    @RequestMapping("/route-blocking")
    public String blockingRouter(
        @RequestParam(value = "minMs", required = false, defaultValue = "0") int minMs,
        @RequestParam(value = "maxMs", required = false, defaultValue = "0") int maxMs) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        LOG.logStartBlocking();

        try {
            ResponseEntity<String> result = restTemplate.getForEntity(
                SP_BLOCKING_URL + "?minMs={minMs}&maxMs={maxMs}", String.class, minMs, maxMs);        
    
            // TODO: Handle status codes other than 200...
            status = result.getStatusCode();

            return result.getBody();

        } finally {
            LOG.logEndBlocking(status.value());
        }
    }

    /**
     * Sample usage: curl "http://localhost:9080/route-non-blocking?minMs=1000&maxMs=2000"
     *
     * @param minMs
     * @param maxMs
     * @return
     * @throws IOException
     */
    @RequestMapping("/route-non-blocking")
    public DeferredResult<String> nonBlockingRouter(
        @RequestParam(value = "minMs", required = false, defaultValue = "0") int minMs,
        @RequestParam(value = "maxMs", required = false, defaultValue = "0") int maxMs) throws IOException {


        LOG.logStartNonBlocking();

        DeferredResult<String> deferredResult = new DeferredResult<String>();

        asyncHttpClient.prepareGet(SP_NON_BLOCKING_URL + "?minMs=" + minMs + "&maxMs=" + maxMs).execute(
            new RouterCallback(LOG, deferredResult));

        LOG.logLeaveThreadNonBlocking();

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }

    /**
     * The spring version of asynch http client has two major drawbacks
     * 1. It doesn't work with the code below, no call is made to the SP (probably my fault :-)
     * 2. The call is not executed non-blocking but instead in a separate thread, i.e. it doesn't scale very good...
     * 
     * Due to the scalability issue it is not used but left as documentation on how it can be used given it is change under the hood to being non-blocking
     * 
     * @param minMs y3
     * @param maxMs y3
     * @return y3
     * @throws IOException
     */
    @RequestMapping("/route-non-blocking-spring")
    public DeferredResult<String> nonBlockingRouter_Spring(
        @RequestParam(value = "minMs", required = false, defaultValue = "0") int minMs,
        @RequestParam(value = "maxMs", required = false, defaultValue = "0") int maxMs) throws IOException {

        LOG.logStartNonBlocking();

        DeferredResult<String> deferredResult = new DeferredResult<String>();

        ListenableFuture<ResponseEntity<String>> futureEntity = asyncRestTemplate.getForEntity(
            SP_NON_BLOCKING_URL + "?minMs={minMs}&maxMs={maxMs}", String.class, minMs, maxMs);        

        // Register a callback for the completion of the asynchronous rest call
        futureEntity.addCallback(new RouterCallback_Spring_AsyncRestTemplate(LOG, deferredResult));

        LOG.logLeaveThreadNonBlocking();

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }
}