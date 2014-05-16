package se.callista.springmvc.asynch.routingslip.statemachine;

import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by magnus on 15/05/14.
 */
public class State {

    public enum ProcessType { SYNCH_PROCESS, ASYNCH_PROCESS };

    public ProcessType processType;
    public int sequenceNo;

    public DeferredResult<String> deferredResult;
    public String temporaryResult;

    public long reqId;
    public AtomicLong concurrentRequests;

    public State(ProcessType processType, long reqId, DeferredResult<String> deferredResult, AtomicLong concurrentRequests) {
        this.processType = processType;
        this.sequenceNo = 0;

        this.deferredResult = deferredResult;
        this.temporaryResult = "";

        this.reqId = reqId;
        this.concurrentRequests = concurrentRequests;
    }
}
