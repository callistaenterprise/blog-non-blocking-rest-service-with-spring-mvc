package se.callista.springmvc.asynch.pattern.routingslip.blocking.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.callista.springmvc.asynch.common.deferredresult.DeferredResultStateMachineCallback;
import se.callista.springmvc.asynch.common.deferredresult.DeferredResultWithBlockingWait;
import se.callista.springmvc.asynch.common.log.LogHelper;
import se.callista.springmvc.asynch.common.log.LogHelperFactory;
import se.callista.springmvc.asynch.common.statemachine.StateMachine;

import javax.annotation.PostConstruct;

@RestController
public class RoutingSlipBlockingStateMachineController {

    private LogHelper LOG;

    @Autowired
    private RoutingSlipBlockingConfiguration configuration;

    @Autowired
    private StateMachine stateMachine;

    @Autowired
    private LogHelperFactory logFactory;

    @PostConstruct
    public void initAfterInject() {
        LOG = logFactory.getLog(RoutingSlipBlockingStateMachineController.class, "routing-slip");
    }

    /**
     * Sample usage: curl "http://localhost:9080/routing-slip-blocking-state-machine"
     *
     * @return
     */
    @RequestMapping("/routing-slip-blocking-state-machine")
    public String blockingRoutingSlip() {

        HttpStatus status = HttpStatus.OK;
        LOG.logStartBlocking();

        try {
            // Create a deferred result that we can wait for in blocking mode
            final DeferredResultWithBlockingWait<String> deferredResult = new DeferredResultWithBlockingWait<>();

            // Kick off the processing
            stateMachine.initProcessing(configuration.getProcessingSteps(), LOG, new DeferredResultStateMachineCallback(deferredResult));

            // Wait (blocking) for its completion
            deferredResult.await();

            // Return the result
            return deferredResult.getResult().toString();

        } finally {
            LOG.logEndBlocking(status.value());
        }
    }
}