package se.callista.springmvc.asynch.routingslip.processors;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.callista.springmvc.asynch.routingslip.statemachine.State;
import se.callista.springmvc.asynch.routingslip.statemachine.StateMachine;

public class AsynchProcessorCallback extends AsyncCompletionHandler<Response> {

    private static final Logger LOG = LoggerFactory.getLogger(AsynchProcessorCallback.class);

    private final StateMachine stateMachine;
    private final State state;

//    private long reqId;
//    private AtomicLong concurrentRequests;
//    private DeferredResult<String> deferredResult;

    public AsynchProcessorCallback(StateMachine stateMachine, State state) {
        this.stateMachine = stateMachine;
        this.state = state;
    }

    @Override
    public Response onCompleted(Response response) throws Exception{

        long concReqs = state.concurrentRequests.getAndDecrement();
        
        // TODO: Handle status codes other than 200...
        int httpStatus = response.getStatusCode();

        if (state.deferredResult.isSetOrExpired()) {
            LOG.warn("{}: Processing of non-blocking routing slip #{} already expired", concReqs, state.reqId);
        } else {
            LOG.debug("Asynch call complete, hand over to the state machine for the next action");
            state.temporaryResult += response.getResponseBody() + '\n';
            stateMachine.processStateComplete(state);

//            boolean deferredStatus = deferredResult.setResult(response.getResponseBody());
//            LOG.debug("{}: Processing of non-blocking routing #{} done, http-status = {}, deferredStatus = {}", concReqs, reqId, httpStatus, deferredStatus);
        }
        return response;
    }

    @Override
    public void onThrowable(Throwable t){

        long concReqs = state.concurrentRequests.getAndDecrement();
        
        // TODO: Handle asynchronous processing errors...

        if (state.deferredResult.isSetOrExpired()) {
            LOG.warn("{}: Processing of non-blocking routing #{} caused an exception: {}", concReqs, state.reqId, t);
        }
    }
}