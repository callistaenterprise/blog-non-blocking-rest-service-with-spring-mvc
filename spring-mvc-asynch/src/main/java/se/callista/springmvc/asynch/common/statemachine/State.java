package se.callista.springmvc.asynch.common.statemachine;

import se.callista.springmvc.asynch.common.log.LogHelper;
import se.callista.springmvc.asynch.common.statemachine.internal.StateProcessingStepIterator;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by magnus on 15/05/14.
 */
public class State {

    private static final AtomicLong lastProcessId = new AtomicLong(0);

    private StateProcessingStepIterator processingSteps;
    private long processId;
    private LogHelper log;
    private StateMachineCallback completionCallback;
    private String result = "";

    public State(Iterator<Processor> processingSteps, LogHelper log, StateMachineCallback completionCallback) {
        this.processingSteps = new StateProcessingStepIterator(processingSteps);
        this.log = log;
        this.completionCallback = completionCallback;
        this.processId = lastProcessId.incrementAndGet();
    }

    public LogHelper getLog() {
        return log;
    }
    public long getProcessId() {
        return processId;
    }

    public Iterator<Processor> getProcessingSteps() {
        return processingSteps;
    }
    public int getProcessingStepNo() { return processingSteps.getProcessingStepNo(); }

    public StateMachineCallback getCompletionCallback() {
        return completionCallback;
    }

    public String appendResult(String newResult) {
        return result += newResult;
    }
    public String getResult() {
        return result;
    }
}
