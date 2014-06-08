package se.callista.springmvc.asynch.common.statemachine;

/**
 * Created by magnus on 29/05/14.
 */
public interface StateMachineCallback {
    void onCompleted(State state);
    void onFailure(State state, Throwable t);
}
