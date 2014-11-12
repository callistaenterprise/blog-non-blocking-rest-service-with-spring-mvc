package se.callista.springmvc.asynch.pattern.aggregator.nonblocking.lambda;

import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.common.lambdasupport.AsyncHttpClientLambdaAware;
import se.callista.springmvc.asynch.common.log.LogHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by magnus on 20/07/14.
 */
public class Executor {

    final private LogHelper log;
    final private String baseUrl;
    final private int timeoutMs;
    final private TaskExecutor dbThreadPoolExecutor;
    final private int dbLookupMs;
    final private int dbHits;
    final private int minMs;
    final private int maxMs;

    private static final AsyncHttpClientLambdaAware asyncHttpClient = new AsyncHttpClientLambdaAware();
    private static Timer timer = new Timer();
    private TimerTask timeoutTask = null;
    private List<ListenableFuture<Response>> concurrentExecutors = new ArrayList<>();
    final AtomicInteger noOfResults = new AtomicInteger(0);
    private int noOfCalls = 0;
    private DeferredResult<String> deferredResult = null;
    private List<String> resultArr = new ArrayList<>();

    public Executor(LogHelper log, String baseUrl, int timeoutMs, TaskExecutor dbThreadPoolExecutor, int dbLookupMs, int dbHits, int minMs, int maxMs) {
        this.log = log;
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
        this.dbThreadPoolExecutor = dbThreadPoolExecutor;
        this.dbLookupMs = dbLookupMs;
        this.dbHits = dbHits;
        this.minMs = minMs;
        this.maxMs = maxMs;
    }

    public DeferredResult<String> startNonBlockingExecution() {
        log.logStartNonBlocking();

        DbLookup dbLookup = new DbLookup(log, dbLookupMs, dbHits);
        deferredResult = new DeferredResult<String>();

        dbThreadPoolExecutor.execute(() -> {
            noOfCalls = dbLookup.executeDbLookup();
            try {
                for (int i = 0; i < noOfCalls; i++) {
                    final int id = i+1;

                    log.logStartProcessingStepNonBlocking(id);
                    String url = baseUrl + "?minMs=" + minMs + "&maxMs=" + maxMs;
                    concurrentExecutors.add(asyncHttpClient.execute(url, (response) -> {

                        // TODO: Handle status codes other than 200...
                        int httpStatus = response.getStatusCode();
                        log.logEndProcessingStepNonBlocking(id, httpStatus);

                        // If many requests completes at the same time the following code must be executed in sequence for one thread at a time
                        // Since we don't have any Actor-like mechanism to rely on (for the time being...) we simply ensure that the code block is executed by one thread at a time by an old school synchronized block
                        // Since the processing in the block is very limited it will not cause a bottleneck.
                        synchronized (resultArr) {
                            // Count down, aggregate answer and return if all answers (also cancel timer)...
                            int noOfRes = noOfResults.incrementAndGet();

                            // Perform the aggregation...
                            log.logMessage("Safely adding response #" + id);
                            resultArr.add(response.getResponseBody());

                            if (noOfRes >= noOfCalls) {
                                onAllCompleted();
                            }
                        }
                        return response;
                    }));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Setup a timer for the max wait-period
            log.logMessage("Start timer for: " + timeoutMs + " ms.");
            timeoutTask = new TimerTask() {

                @Override
                public void run() {
                    onTimeout();
                }
            };

            // Schedule the timeout task
            timer.schedule(timeoutTask, timeoutMs);

        });

        // Ok, everything is now setup for asynchronous processing, let the play begin...
        log.logLeaveThreadNonBlocking();

        return deferredResult;
    }

    private void onTimeout() {

        // complete missing answers and return ...
        log.logMessage("Timeout in aggregating service, only received answers from " + noOfResults.get() + " out of total " + noOfCalls + " expected responses.");
        int i = 0;
        for (ListenableFuture<Response> executor : concurrentExecutors) {
            if (!executor.isDone()) {
                log.logMessage("Cancel asych request #" + i);
                executor.cancel(true);
            }
            i++;
        }
        onAllCompleted();
    }

    private void onAllCompleted() {
        log.logMessage("All done, cancel timer");
        timeoutTask.cancel();

        if (deferredResult.isSetOrExpired()) {
            log.logAlreadyExpiredNonBlocking();
        } else {
            boolean deferredStatus = deferredResult.setResult(getTotalResult());
            log.logEndNonBlocking(200, deferredStatus);
        }
    }

    private String getTotalResult() {
        String totalResult = "";
        for (String r : resultArr)
            totalResult += r + '\n';
        return totalResult;
    }

}
