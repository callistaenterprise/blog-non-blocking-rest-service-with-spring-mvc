package se.callista.springmvc.asynch.pattern.routingslip.nonblocking.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.common.deferredresult.DeferredResultStateMachineCallback;
import se.callista.springmvc.asynch.common.log.LogHelper;
import se.callista.springmvc.asynch.common.log.LogHelperFactory;
import se.callista.springmvc.asynch.common.statemachine.Processor;
import se.callista.springmvc.asynch.common.statemachine.StateMachine;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Iterator;

@RestController
public class RoutingSlipNonBlockingStateMachineController {

    private LogHelper LOG;

    @Autowired
    private RoutingSlipNonBlockingConfiguration configuration;

    @Autowired
    private StateMachine stateMachine;

    @Autowired
    private LogHelperFactory logFactory;

    @PostConstruct
    public void initAfterInject() {
        LOG = logFactory.getLog(RoutingSlipNonBlockingStateMachineController.class, "routing-slip");
    }

    /**
     * Sample usage: curl "http://localhost:9080/routing-slip-non-blocking-state-machine"
     *
     * @return
     */
    @RequestMapping("/routing-slip-non-blocking-state-machine")
    public DeferredResult<String> nonBlockingRoutingSlip() throws IOException {

        LOG.logStartNonBlocking();

        // Create a deferred result
        final DeferredResult<String> deferredResult = new DeferredResult<>();

        // Kick off the asynch processing of a number of sequentially executed asynch processing steps
        Iterator<Processor> processingSteps = configuration.getProcessingSteps();

        stateMachine.initProcessing(processingSteps, LOG, new DeferredResultStateMachineCallback(deferredResult));

        LOG.logLeaveThreadNonBlocking();

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }

}