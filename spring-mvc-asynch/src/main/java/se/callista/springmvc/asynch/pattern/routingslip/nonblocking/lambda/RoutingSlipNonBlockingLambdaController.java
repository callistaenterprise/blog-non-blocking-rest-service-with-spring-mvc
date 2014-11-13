package se.callista.springmvc.asynch.pattern.routingslip.nonblocking.lambda;

import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.common.lambdasupport.AsyncHttpClientLambdaAware;
import se.callista.springmvc.asynch.common.log.LogHelper;
import se.callista.springmvc.asynch.common.log.LogHelperFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class RoutingSlipNonBlockingLambdaController {

    private class Result {
        List<String> resultList = new ArrayList<>();

        public void processResult(String result) {
            resultList.add(result);
        }

        public String getTotalResult() {
            String totalResult = "";
            for (String r : resultList)
                totalResult += r + '\n';
            return totalResult;
        }
    }

    private LogHelper LOG;

    private static final AsyncHttpClientLambdaAware asyncHttpClient = new AsyncHttpClientLambdaAware();

    @Autowired
    private LogHelperFactory logFactory;

    @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    @PostConstruct
    public void initAfterInject() {
        LOG = logFactory.getLog(se.callista.springmvc.asynch.pattern.routingslip.nonblocking.lambda.RoutingSlipNonBlockingLambdaController.class, "routing-slip");
    }

    /**
     * Sample usage: curl "http://localhost:9181/routing-slip-non-blocking-lambds"
     *
     * @return
     */
    @RequestMapping("/routing-slip-non-blocking-lambda")
    public DeferredResult<String> nonBlockingRoutingSlip() throws IOException {

        LOG.logStartNonBlocking();

        DeferredResult<String> deferredResult = new DeferredResult<>();
        Result r = new Result();

        // Kick off the asynch processing of five sequentially executed asynch processing steps

        // Send request #1
        LOG.logStartProcessingStepNonBlocking(1);
        ListenableFuture<Response> execute = asyncHttpClient.execute(getUrl(1),
                (Response r1) -> {
                    LOG.logEndProcessingStepNonBlocking(1, r1.getStatusCode());

                    // Process response #1
                    r.processResult(r1.getResponseBody());

                    // Send request #2
                    LOG.logStartProcessingStepNonBlocking(2);
                    asyncHttpClient.execute(getUrl(2),
                            (Response r2) -> {
                                LOG.logEndProcessingStepNonBlocking(2, r2.getStatusCode());

                                // Process response #2
                                r.processResult(r2.getResponseBody());

                                // Send request #3
                                LOG.logStartProcessingStepNonBlocking(3);
                                asyncHttpClient.execute(getUrl(3),
                                        (Response r3) -> {
                                            LOG.logEndProcessingStepNonBlocking(3, r3.getStatusCode());

                                            // Process response #3
                                            r.processResult(r3.getResponseBody());

                                            // Send request #4
                                            LOG.logStartProcessingStepNonBlocking(4);
                                            asyncHttpClient.execute(getUrl(4),
                                                    (Response r4) -> {
                                                        LOG.logEndProcessingStepNonBlocking(4, r4.getStatusCode());

                                                        // Process response #4
                                                        r.processResult(r4.getResponseBody());

                                                        // Send request #5
                                                        LOG.logStartProcessingStepNonBlocking(5);
                                                        asyncHttpClient.execute(getUrl(5),
                                                            (Response r5) -> {
                                                                LOG.logEndProcessingStepNonBlocking(5, r5.getStatusCode());

                                                                // Process response #5
                                                                r.processResult(r5.getResponseBody());

                                                                // Get the total result and set it on the deferred result
                                                                boolean deferredStatus = deferredResult.setResult(r.getTotalResult());
                                                                LOG.logEndNonBlocking(r5.getStatusCode(), deferredStatus);

                                                                return r5;
                                                            });
                                                        return r4;
                                                    });
                                            return r3;
                                        });
                                return r2;
                            });
                    return r1;
                });

        LOG.logLeaveThreadNonBlocking();

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }

    private String getUrl(int processingStepNo) {
        int sleeptimeMs = 100 * processingStepNo;
        return SP_NON_BLOCKING_URL + "?minMs=" + sleeptimeMs + "&maxMs=" + sleeptimeMs;
    }
}