package se.callista.springmvc.asynch.pattern.aggregator.nonblocking.callback;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.common.log.LogHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by magnus on 22/04/14.
 */
public class AggregatorEventHandler {

    private Timer timer = new Timer();

    private final LogHelper log;

//     @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    private final int noOfCalls;
    private final int maxMs;
    private final int minMs;
    private final int timeoutMs;
    private final DeferredResult<String> deferredResult;

    private final static AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    private final AtomicInteger noOfResults = new AtomicInteger(0);
    private String result = "";

    private List<ListenableFuture<Response>> executors = new ArrayList<>();

    public AggregatorEventHandler(LogHelper log, int noOfCalls, String url, int minMs, int maxMs, int timeoutMs, DeferredResult<String> deferredResult) {
        this.log = log;
        this.noOfCalls = noOfCalls;
        this.SP_NON_BLOCKING_URL = url;
        this.minMs = minMs;
        this.maxMs = maxMs;
        this.timeoutMs = timeoutMs;
        this.deferredResult = deferredResult;
    }

    public void onStart() {
        try {
            for (int i = 0; i < noOfCalls; i++) {
                log.logStartProcessingStepNonBlocking(i);
                String url = SP_NON_BLOCKING_URL + "?minMs=" + minMs + "&maxMs=" + maxMs;
                executors.add(asyncHttpClient.prepareGet(url).execute(new AggregatorCallback(i, this)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Setup a timer for the max wait-period
        log.logMessage("Start timer for: " + timeoutMs + " ms.");
        TimerTask timeoutTask = new TimerTask() {

            @Override
            public void run() {
                onTimeout();
            }
        };

        // Schedule the timeout task
        timer.schedule(timeoutTask, timeoutMs);
    }

    public void onResult(int id, Response response) {

        try {
            // TODO: Handle status codes other than 200...
            int httpStatus = response.getStatusCode();
            log.logEndProcessingStepNonBlocking(id, httpStatus);

            // If many requests completes at the same time the following code must be executed in sequence for one thread at a time
            // Since we don't have any Actor-like mechanism to rely on (for the time being...) we simply ensure that the code block is executed by one thread at a time by an old school synchronized block
            // Since the processing in the block is very limited it will not cause a bottleneck.
            synchronized (result) {
                // Count down, aggregate answer and return if all answers (also cancel timer)...
                int noOfRes = noOfResults.incrementAndGet();

                // Perform the aggregation...
                log.logMessage("Safely adding response #" + id);
                result += response.getResponseBody() + '\n';

                if (noOfRes >= noOfCalls) {
                    onAllCompleted();
                }
            }



        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onError(int id, Throwable t) {

        // Skip logging an error if we just canceled the request due to an timeout
        if (t instanceof CancellationException) return;

        log.logExceptionNonBlocking(t);

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
        log.logMessage("Timeout in aggregating service, only received answers from " + noOfResults.get() + " out of total " + noOfCalls + " expected responses.");
        int i = 0;
        for (ListenableFuture<Response> executor : executors) {
            if (!executor.isDone()) {
                log.logMessage("Cancel asych request #" + i);
                executor.cancel(true);
            }
            i++;
        }
        onAllCompleted();
    }

    public void onAllCompleted() {
        log.logMessage("All done, cancel timer");
        timer.cancel();

        if (deferredResult.isSetOrExpired()) {
            log.logAlreadyExpiredNonBlocking();
        } else {
            boolean deferredStatus = deferredResult.setResult(result);
            log.logEndNonBlocking(200, deferredStatus);
        }
    }
}