package se.callista.springmvc.asynch.routingslip;

import static se.callista.springmvc.asynch.routingslip.statemachine.ProcessType.ASYNCH_PROCESS;
import static se.callista.springmvc.asynch.routingslip.statemachine.ProcessType.SYNCH_PROCESS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.routingslip.processors.AsynchProcessor;
import se.callista.springmvc.asynch.routingslip.processors.SynchProcessor;
import se.callista.springmvc.asynch.routingslip.statemachine.*;
import se.callista.springmvc.asynch.routingslip.util.DeferredResultWithBlockingWait;
import se.callista.springmvc.asynch.util.LogHelper;
import se.callista.springmvc.asynch.util.LogHelperFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
public class RoutingSlipController {
    
    private LogHelper LOG;

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
            stateMachine.initProcessing(getProcessingStepsFromConfiguration(SYNCH_PROCESS), LOG, new StateMachineCallback() {
                @Override
                public void onCompleted(State result) {
                    boolean deferredStatus = deferredResult.setResult(result.getResult());
                }

                @Override
                public void onFailure(Throwable t) {
                    deferredResult.setErrorResult(t);
                    LOG.logExceptionBlocking(t);
                }
            });

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
        stateMachine.initProcessing(getProcessingStepsFromConfiguration(ASYNCH_PROCESS), LOG, new StateMachineCallback() {
            @Override
            public void onCompleted(State result) {
                if (deferredResult.isSetOrExpired()) {
                    LOG.logAlreadyExpiredNonBlocking();

                } else {
                    boolean deferredStatus = deferredResult.setResult(result.getResult());
                    LOG.logEndNonBlocking(200, deferredStatus);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                deferredResult.setErrorResult(t);
                LOG.logExceptionNonBlocking(t);
            }
        });

        LOG.logLeaveThreadNonBlocking();

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }

    @Autowired
    private AsynchProcessor asynchProcessor;

    @Autowired
    private SynchProcessor synchProcessor;

    /**
     * Simulates setting up a number of processing steps from some kind of configuration...
     *
     * @param processType
     * @return
     */
    private Iterator<Processor> getProcessingStepsFromConfiguration(ProcessType processType) {

        List<Processor> processingSteps = new ArrayList<Processor>();
        for (int i = 0; i < 5; i++) {
            switch (processType) {
                case ASYNCH_PROCESS:
                    processingSteps.add(asynchProcessor);
                    break;
                case SYNCH_PROCESS:
                    processingSteps.add(synchProcessor);
                    break;
                default:
                    throw new RuntimeException("Unknown process type: " + processType);
            }
        }
        return processingSteps.iterator();
    }
}