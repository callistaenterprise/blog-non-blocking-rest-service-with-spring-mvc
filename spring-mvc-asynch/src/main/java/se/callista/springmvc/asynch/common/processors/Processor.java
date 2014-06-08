package se.callista.springmvc.asynch.common.processors;

import se.callista.springmvc.asynch.common.statemachine.State;

/**
 * Created by magnus on 15/05/14.
 */
public interface Processor {
    public void process(State state);
}
