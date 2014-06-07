package se.callista.springmvc.asynch.router;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import se.callista.springmvc.asynch.util.LogHelper;

public class RouterCallback extends AsyncCompletionHandler<Response> {

    private final LogHelper log;

    private DeferredResult<String> deferredResult;


    public RouterCallback(LogHelper log, DeferredResult<String> deferredResult) {
        this.log = log;
        this.deferredResult = deferredResult;
    }

    @Override
    public Response onCompleted(Response response) throws Exception{

        // TODO: Handle status codes other than 200...
        int httpStatus = response.getStatusCode();

        if (deferredResult.isSetOrExpired()) {
            log.logAlreadyExpiredNonBlocking();

        } else {
            boolean deferredStatus = deferredResult.setResult(response.getResponseBody());
            log.logEndNonBlocking(httpStatus, deferredStatus);
        }
        return response;
    }

    @Override
    public void onThrowable(Throwable t){

        // TODO: Handle asynchronous processing errors...

        if (deferredResult.isSetOrExpired()) {
            log.logExceptionNonBlocking(t);
        }
    }
}