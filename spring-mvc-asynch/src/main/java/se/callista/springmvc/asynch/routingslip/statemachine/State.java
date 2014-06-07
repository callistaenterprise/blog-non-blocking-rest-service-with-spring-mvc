package se.callista.springmvc.asynch.routingslip.statemachine;

import se.callista.springmvc.asynch.util.LogHelper;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by magnus on 15/05/14.
 */
public class State {

    private static final AtomicLong lastProcessId = new AtomicLong(0);

    private Iterator<Processor> processingSteps;
    private int processingStepNo;
    private long processId;
    private LogHelper log;
    private StateMachineCallback completionCallback;
    private String result;

    public State(Iterator<Processor> processingSteps, LogHelper log, StateMachineCallback completionCallback) {
        this.processingSteps = processingSteps;
        this.log = log;
        this.completionCallback = completionCallback;

        this.processingStepNo = 0;
        this.processId = lastProcessId.incrementAndGet();
        this.result = "";
    }

    public Iterator<Processor> getProcessingSteps() {
        return processingSteps;
    }

    public int getProcessingStepNo() {
        return processingStepNo;
    }

    public long getProcessId() {
        return processId;
    }

    public LogHelper getLog() {
        return log;
    }

    public StateMachineCallback getCompletionCallback() {
        return completionCallback;
    }

    public String getResult() {
        return result;
    }

    public int incrementProcessingStepNo() {
        return ++processingStepNo;
    }

    public String appendResult(String newResult) {
        return result += newResult;

    }

}
