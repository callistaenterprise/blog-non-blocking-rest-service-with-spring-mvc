package se.callista.springmvc.asynch.pattern.aggregator;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;

/**
 * Created by magnus on 22/04/14.
 */
public class AggregatorEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AggregatorEventHandler.class);

    @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    private final int noOfCalls;
    private final int maxMs;
    private final int minMs;
    private final DeferredResult<String> deferredResult;

    private final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public AggregatorEventHandler(int noOfCalls, int minMs, int maxMs, DeferredResult<String> deferredResult) {
        this.noOfCalls = noOfCalls;
        this.minMs = minMs;
        this.maxMs = maxMs;
        this.deferredResult = deferredResult;
        boolean deferredStatus = deferredResult.setResult("NOT YET IMPLEMENTED");
    }

    public void onStart() {
        try {
// FIXME. ML           for...

            asyncHttpClient.prepareGet(SP_NON_BLOCKING_URL + "?minMs=" + minMs + "&maxMs=" + maxMs).execute(
                new AggregatorCallback(this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

// FIXME. ML        kickoff a timer as well + konfiguration...
    }

    public void onResult(Response response) {

        // Count down, aggregate answer and return if all answers (also cancel timer)...

        // TODO: Handle status codes other than 200...
        int httpStatus = response.getStatusCode();

        if (deferredResult.isSetOrExpired()) {
// FIXME. ML            LOG.warn("{}: Processing of non-blocking routing #{} already expired", concReqs, reqId);
        } else {
// FIXME. ML            boolean deferredStatus = deferredResult.setResult(response.getResponseBody());
// FIXME. ML            LOG.debug("{}: Processing of non-blocking routing #{} done, http-status = {}, deferredStatus = {}", concReqs, reqId, httpStatus, deferredStatus);
        }

    }

    public void onError(Throwable t) {

        // Count down, aggregate answer and return if all answers (also cancel timer)...

        // TODO: Handle asynchronous processing errors...

        if (deferredResult.isSetOrExpired()) {
// FIXME. ML            LOG.warn("{}: Processing of non-blocking routing #{} caused an exception: {}", concReqs, reqId, t);
        }
    }

    public void onTimeout() {

        // complete missing answers and return ...

    }
}
