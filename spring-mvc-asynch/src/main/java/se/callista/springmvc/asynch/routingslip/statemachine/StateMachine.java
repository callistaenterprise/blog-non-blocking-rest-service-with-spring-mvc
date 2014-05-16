package se.callista.springmvc.asynch.routingslip.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.routingslip.processors.AsynchProcessor;
import se.callista.springmvc.asynch.routingslip.processors.SynchProcessor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by magnus on 15/05/14.
 */
@Component
public class StateMachine {

    private static final Logger LOG = LoggerFactory.getLogger(StateMachine.class);

    @Autowired
    private AsynchProcessor asynchProcessor;

    @Autowired
    private SynchProcessor synchProcessor;

    public void initProcessing(State.ProcessType processType, long reqId, DeferredResult<String> deferredResult, AtomicLong concurrentRequests) {
        processStateComplete(new State(processType, reqId, deferredResult, concurrentRequests));
    }

    public void processStateComplete(State state) {

        if (state.sequenceNo++ < 5) {
            // Initiate next processing step...
            getProcessorFromConfiguration(state).process(state);

        } else {
            // We are done...
            boolean deferredStatus = state.deferredResult.setResult(state.temporaryResult);
            LOG.debug("{}: Processing of routing slip #{} done, deferredStatus = {}, result = {}", state.concurrentRequests.decrementAndGet(), state.reqId, deferredStatus, state.temporaryResult);
        }
    }

    private Processor getProcessorFromConfiguration(State state) {
        switch (state.processType) {
            case ASYNCH_PROCESS:
                return asynchProcessor;
            case SYNCH_PROCESS:
                return synchProcessor;
            default:
                throw new RuntimeException("Unknown process type: " + state.processType);
        }
    }
}
