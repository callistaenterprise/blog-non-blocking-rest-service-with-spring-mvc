package se.callista.springmvc.asynch.pattern.routingslip;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.callista.springmvc.asynch.common.processors.AsynchProcessor;
import se.callista.springmvc.asynch.common.processors.Processor;
import se.callista.springmvc.asynch.common.processors.SynchProcessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by magnus on 08/06/14.
 */
@Component
public class RoutingSlipConfiguration {
    protected enum ProcessType { SYNCH_PROCESS, ASYNCH_PROCESS }

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
    public Iterator<Processor> getProcessingSteps(ProcessType processType) {

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
