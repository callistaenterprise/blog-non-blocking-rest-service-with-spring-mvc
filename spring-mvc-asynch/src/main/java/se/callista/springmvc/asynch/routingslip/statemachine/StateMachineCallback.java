package se.callista.springmvc.asynch.routingslip.statemachine;

/**
 * Created by magnus on 29/05/14.
 */
public interface StateMachineCallback {
    void onCompleted(State result);
    void onFailure(Throwable t);
}
