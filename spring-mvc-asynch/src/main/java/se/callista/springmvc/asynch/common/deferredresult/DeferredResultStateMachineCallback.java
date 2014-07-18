package se.callista.springmvc.asynch.common.deferredresult;

import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.common.statemachine.State;
import se.callista.springmvc.asynch.common.statemachine.StateMachineCallback;

/**
 * Created by magnus on 08/06/14.
 */
public class DeferredResultStateMachineCallback implements StateMachineCallback {

    private DeferredResult deferredResult;

    public DeferredResultStateMachineCallback(DeferredResult deferredResult) {
        this.deferredResult = deferredResult;
    }

    @Override
    public void onCompleted(State state) {
        if (deferredResult.isSetOrExpired()) {
            state.getLog().logAlreadyExpiredNonBlocking();

        } else {
            boolean deferredStatus = deferredResult.setResult(state.getResult());
            state.getLog().logEndNonBlocking(200, deferredStatus);
        }
    }

    @Override
    public void onFailure(State state, Throwable t) {
        deferredResult.setErrorResult(t);
        state.getLog().logExceptionNonBlocking(t);
    }
}