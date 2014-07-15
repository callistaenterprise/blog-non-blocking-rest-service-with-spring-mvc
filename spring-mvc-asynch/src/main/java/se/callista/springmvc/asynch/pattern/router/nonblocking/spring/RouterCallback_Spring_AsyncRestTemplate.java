package se.callista.springmvc.asynch.pattern.router.nonblocking.spring;

import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.common.log.LogHelper;

public class RouterCallback_Spring_AsyncRestTemplate implements ListenableFutureCallback<ResponseEntity<String>> {

    private LogHelper log;
    private long reqId;
    private DeferredResult<String> deferredResult;

    public RouterCallback_Spring_AsyncRestTemplate(LogHelper log, DeferredResult<String> deferredResult) {
        this.log = log;
        this.reqId = reqId;
        this.deferredResult = deferredResult;
    }

    @Override
    public void onSuccess(ResponseEntity<String> result) {

        // TODO: Handle status codes other than 200...

        if (deferredResult.isSetOrExpired()) {
            log.logAlreadyExpiredNonBlocking();
        } else {
            boolean deferredStatus = deferredResult.setResult(result.getBody());
            log.logEndNonBlocking(result.getStatusCode().value(), deferredStatus);
        }
        
    }

    @Override
    public void onFailure(Throwable t) {

        // TODO: Handle asynchronous processing errors...

        if (deferredResult.isSetOrExpired()) {
            log.logExceptionNonBlocking(t);
        }
    }
}
