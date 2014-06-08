package se.callista.springmvc.asynch.pattern.aggregator;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by magnus on 22/04/14.
 */
public class AggregatorEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AggregatorEventHandler.class);

//     @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    private final int noOfCalls;
    private final int maxMs;
    private final int minMs;
    private final DeferredResult<String> deferredResult;

    private final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    private final AtomicInteger noOfResults = new AtomicInteger(0);
    private String result = "";

    public AggregatorEventHandler(int noOfCalls, String url, int minMs, int maxMs, DeferredResult<String> deferredResult) {
        this.noOfCalls = noOfCalls;
        this.SP_NON_BLOCKING_URL = url;
        this.minMs = minMs;
        this.maxMs = maxMs;
        this.deferredResult = deferredResult;
    }

    public void onStart() {
        try {
            LOG.debug("{}: Start processing of {} non-blocking aggregations in request #{}", "???", noOfCalls, "???");
            for (int i = 0; i < noOfCalls; i++) {
                String url = SP_NON_BLOCKING_URL + "?minMs=" + minMs + "&maxMs=" + maxMs;
                asyncHttpClient.prepareGet(url).execute(new AggregatorCallback(i, this));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // FIXME. ML. kickoff a timer as well + konfiguration...
    }

    public void onResult(int id, Response response) {

        try {
            // TODO: Handle status codes other than 200...
            int httpStatus = response.getStatusCode();

            LOG.debug("{}: Processing of non-blocking aggregation #{}.{} returned http status: {}", "???", "???", id, httpStatus);

            // Count down, aggregate answer and return if all answers (also cancel timer)...
            int noOfRes = noOfResults.incrementAndGet();
            result += response.getResponseBody() + '\n';

            if (noOfRes >= noOfCalls) {
                onAllCompleted();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onError(int id, Throwable t) {

        LOG.warn("{}: Processing of non-blocking aggregation #{}.{} caused an exception: {}", "???", "???", id,  t);

        // Count down, aggregate answer and return if all answers (also cancel timer)...
        int noOfRes = noOfResults.incrementAndGet();
        result += t.toString() + '\n';

        if (noOfRes >= noOfCalls) {
            onAllCompleted();
        }

        // TODO: Handle asynchronous processing errors...

    }

    public void onTimeout() {

        // complete missing answers and return ...

    }

    public void onAllCompleted() {
        if (deferredResult.isSetOrExpired()) {
            LOG.warn("{}: Processing of non-blocking aggregation #{} already expired", "???", "???");
        } else {
            boolean deferredStatus = deferredResult.setResult(result);
            LOG.debug("{}: Processing of non-blocking aggregation #{} done, deferredStatus = {}", "???", "???", deferredStatus);
        }
    }
}