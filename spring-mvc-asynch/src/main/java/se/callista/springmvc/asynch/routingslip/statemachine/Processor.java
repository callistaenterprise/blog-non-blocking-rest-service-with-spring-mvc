package se.callista.springmvc.asynch.routingslip.statemachine;

/**
 * Created by magnus on 15/05/14.
 */
public interface Processor {
    public void process(State state);
}
