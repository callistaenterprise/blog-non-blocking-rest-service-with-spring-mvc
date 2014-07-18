package se.callista.springmvc.asynch.common.statemachine.internal;

import se.callista.springmvc.asynch.common.statemachine.Processor;

import java.util.Iterator;

/**
 * Created by magnus on 08/06/14.
 */
public class StateProcessingStepIterator implements Iterator<Processor> {

    private final Iterator<Processor> processingSteps;
    private int processingStepNo = 0;

    public StateProcessingStepIterator(Iterator<Processor> processingSteps) {
        this.processingSteps = processingSteps;
    }

    @Override
    public boolean hasNext() {
        return processingSteps.hasNext();
    }

    @Override
    public Processor next() {
        if (processingSteps.hasNext()) processingStepNo++;
        return processingSteps.next();
    }

    public int getProcessingStepNo() {
        return processingStepNo;
    }

    @Override
    public void remove() {
        processingSteps.remove();
    }
}
