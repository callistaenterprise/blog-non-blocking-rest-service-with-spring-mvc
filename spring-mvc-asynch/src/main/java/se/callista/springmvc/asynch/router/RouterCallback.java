package se.callista.springmvc.asynch.router;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;

public class RouterCallback extends AsyncCompletionHandler<Response> {

    private static final Logger LOG = LoggerFactory.getLogger(RouterCallback.class);

    private long reqId;
    private AtomicLong concurrentRequests;
    private DeferredResult<String> deferredResult;

    public RouterCallback(long reqId, AtomicLong concurrentRequests, DeferredResult<String> deferredResult) {
        this.reqId = reqId;
        this.concurrentRequests = concurrentRequests;
        this.deferredResult = deferredResult;
    }

    @Override
    public Response onCompleted(Response response) throws Exception{

        long concReqs = concurrentRequests.getAndDecrement();
        
        // TODO: Handle status codes other than 200...
        int httpStatus = response.getStatusCode();

        if (deferredResult.isSetOrExpired()) {
            LOG.warn("{}: Processing of non-blocking routing #{} already expired", concReqs, reqId);        
        } else {
            boolean deferredStatus = deferredResult.setResult(response.getResponseBody());
            LOG.debug("{}: Processing of non-blocking routing #{} done, http-status = {}, deferredStatus = {}", concReqs, reqId, httpStatus, deferredStatus);
        }
        return response;
    }

    @Override
    public void onThrowable(Throwable t){

        long concReqs = concurrentRequests.getAndDecrement();
        
        // TODO: Handle asynchronous processing errors...

        if (deferredResult.isSetOrExpired()) {
            LOG.warn("{}: Processing of non-blocking routing #{} caused an exception: {}", concReqs, reqId, t);        
        }
    }
}