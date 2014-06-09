package se.callista.springmvc.asynch.pattern.routingslip;

import static se.callista.springmvc.asynch.pattern.routingslip.RoutingSlipConfiguration.ProcessType.ASYNCH_PROCESS;
import static se.callista.springmvc.asynch.pattern.routingslip.RoutingSlipConfiguration.ProcessType.SYNCH_PROCESS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.common.deferredresult.DeferredResultStateMachineCallback;
import se.callista.springmvc.asynch.common.statemachine.*;
import se.callista.springmvc.asynch.common.deferredresult.DeferredResultWithBlockingWait;
import se.callista.springmvc.asynch.common.log.LogHelper;
import se.callista.springmvc.asynch.common.log.LogHelperFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;

@RestController
public class RoutingSlipController {

    private LogHelper LOG;

    @Autowired
    private RoutingSlipConfiguration configuration;

    @Autowired
    private StateMachine stateMachine;

    @Autowired
    private LogHelperFactory logFactory;

    @PostConstruct
    public void initAfterInject() {
        LOG = logFactory.getLog(RoutingSlipController.class, "routing-slip");
    }

    /**
     * Sample usage: curl "http://localhost:9080/routing-slip-blocking"
     *
     * @return
     */
    @RequestMapping("/routing-slip-blocking")
    public String blockingRoutingSlip() {

        HttpStatus status = HttpStatus.OK;
        LOG.logStartBlocking();

        try {
            // Create a deferred result that we can wait for in blocking mode
            final DeferredResultWithBlockingWait<String> deferredResult = new DeferredResultWithBlockingWait<>();

            // Kick off the processing
            stateMachine.initProcessing(configuration.getProcessingSteps(SYNCH_PROCESS), LOG, new DeferredResultStateMachineCallback(deferredResult));

            // Wait (blocking) for its completion
            deferredResult.await();

            // Return the result
            return deferredResult.getResult().toString();

        } finally {
            LOG.logEndBlocking(status.value());
        }
    }

    /**
     * Sample usage: curl "http://localhost:9080/routing-slip-non-blocking"
     *
     * @return
     */
    @RequestMapping("/routing-slip-non-blocking")
    public DeferredResult<String> nonBlockingRoutingSlip() throws IOException {

        LOG.logStartNonBlocking();

        // Create a deferred result
        final DeferredResult<String> deferredResult = new DeferredResult<>();

        // Kick off the asynch processing of a number of sequentially executed asynch processing steps
        stateMachine.initProcessing(configuration.getProcessingSteps(ASYNCH_PROCESS), LOG, new DeferredResultStateMachineCallback(deferredResult));

        LOG.logLeaveThreadNonBlocking();

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }

}