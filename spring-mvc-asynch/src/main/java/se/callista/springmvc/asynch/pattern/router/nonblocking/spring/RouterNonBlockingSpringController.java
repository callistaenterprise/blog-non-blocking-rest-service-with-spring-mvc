package se.callista.springmvc.asynch.pattern.router.nonblocking.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.common.log.LogHelper;
import se.callista.springmvc.asynch.common.log.LogHelperFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;

@RestController
public class RouterNonBlockingSpringController {

    private LogHelper LOG;

    private AsyncRestTemplate asyncRestTemplate  = new AsyncRestTemplate();

    @Autowired
    private LogHelperFactory logFactory;

    @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    @PostConstruct
    public void initAfterInject() {
        LOG = logFactory.getLog(se.callista.springmvc.asynch.pattern.router.nonblocking.callback.RouterNonBlockingCallbackController.class, "router");
    }

    /**
     * Sample usage: curl "http://localhost:9080/router-non-blocking-spring?minMs=1000&maxMs=2000"
     *
     * The spring version of asynch http client has two major drawbacks
     * 1. It doesn't work with the code below, no call is made to the SP (probably my fault :-)
     * 2. The call is not executed non-blocking but instead in a separate thread, i.e. it doesn't scale very good...
     *
     * Due to the scalability issue it is not used but left as documentation on how it can be used given it is change under the hood to being non-blocking
     *
     * @param minMs y3
     * @param maxMs y3
     * @return y3
     * @throws java.io.IOException
     */
    @RequestMapping("/router-non-blocking-spring")
    public DeferredResult<String> nonBlockingRouter_Spring(
        @RequestParam(value = "minMs", required = false, defaultValue = "0") int minMs,
        @RequestParam(value = "maxMs", required = false, defaultValue = "0") int maxMs) throws IOException {

        LOG.logStartNonBlocking();

        DeferredResult<String> deferredResult = new DeferredResult<String>();

        String url = SP_NON_BLOCKING_URL + "?minMs={minMs}&maxMs={maxMs}";
        ListenableFuture<ResponseEntity<String>> futureEntity = asyncRestTemplate.getForEntity(url, String.class, minMs, maxMs);

        // Register a callback for the completion of the asynchronous rest call
        futureEntity.addCallback(new RouterCallback_Spring_AsyncRestTemplate(LOG, deferredResult));

        LOG.logLeaveThreadNonBlocking();

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }
}