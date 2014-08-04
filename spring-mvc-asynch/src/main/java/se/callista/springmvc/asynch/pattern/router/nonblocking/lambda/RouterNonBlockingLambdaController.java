package se.callista.springmvc.asynch.pattern.router.nonblocking.lambda;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.common.lambdasupport.AsyncHttpClientLambdaAware;
import se.callista.springmvc.asynch.common.log.LogHelper;
import se.callista.springmvc.asynch.common.log.LogHelperFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;

@RestController
public class RouterNonBlockingLambdaController {

    private LogHelper LOG;

    private static final AsyncHttpClientLambdaAware asyncHttpClient = new AsyncHttpClientLambdaAware();

    @Autowired
    private LogHelperFactory logFactory;

    @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    @PostConstruct
    public void initAfterInject() {
        LOG = logFactory.getLog(RouterNonBlockingLambdaController.class, "router");
    }

    /**
     * Sample usage: curl "http://localhost:9080/router-non-blocking-lambda?minMs=1000&maxMs=2000"
     *
     * @param minMs
     * @param maxMs
     * @return
     * @throws java.io.IOException
     */
    @RequestMapping("/router-non-blocking-lambda")
    public DeferredResult<String> nonBlockingRouter(
        @RequestParam(value = "minMs", required = false, defaultValue = "0") int minMs,
        @RequestParam(value = "maxMs", required = false, defaultValue = "0") int maxMs) throws IOException {

        LOG.logStartNonBlocking();

        final DeferredResult<String> deferredResult = new DeferredResult<>();

        String url = SP_NON_BLOCKING_URL + "?minMs=" + minMs + "&maxMs=" + maxMs;

        asyncHttpClient.execute(url,
            (response) ->  {
                // TODO: Handle status codes other than 200...
                int httpStatus = response.getStatusCode();

                if (deferredResult.isSetOrExpired()) {
                    LOG.logAlreadyExpiredNonBlocking();

                } else {
                    boolean deferredStatus = deferredResult.setResult(response.getResponseBody());
                    LOG.logEndNonBlocking(httpStatus, deferredStatus);
                }
                return response;
            },
            (throwable) -> {
                // TODO: Handle asynchronous processing errors...

                if (deferredResult.isSetOrExpired()) {
                    LOG.logExceptionNonBlocking(throwable);
                }
            }
        );

        LOG.logLeaveThreadNonBlocking();

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }
}