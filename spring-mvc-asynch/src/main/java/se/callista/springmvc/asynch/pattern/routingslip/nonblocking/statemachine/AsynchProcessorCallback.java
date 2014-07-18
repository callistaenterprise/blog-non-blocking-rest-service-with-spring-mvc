package se.callista.springmvc.asynch.pattern.routingslip.nonblocking.statemachine;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.callista.springmvc.asynch.common.statemachine.State;
import se.callista.springmvc.asynch.common.statemachine.StateMachine;

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

        // TODO: Handle status codes other than 200...
        // int httpStatus = response.getStatusCode();

        state.getLog().logAsynchProcessingStepComplete();
        state.appendResult(response.getResponseBody() + '\n');
        stateMachine.executeNextStep(state);

        return response;
    }

    @Override
    public void onThrowable(Throwable t){

        // TODO: Handle asynchronous processing errors...
        state.getLog().logExceptionNonBlocking(t);
    }
}