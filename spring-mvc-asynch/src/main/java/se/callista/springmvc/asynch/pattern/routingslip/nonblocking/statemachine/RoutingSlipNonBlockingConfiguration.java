package se.callista.springmvc.asynch.pattern.routingslip.nonblocking.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.callista.springmvc.asynch.common.statemachine.Processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by magnus on 08/06/14.
 */
@Component
public class RoutingSlipNonBlockingConfiguration {

    @Autowired
    private AsynchProcessor asynchProcessor;

    /**
     * Simulates setting up a number of processing steps from some kind of configuration...
     *
     * @return
     */
    public Iterator<Processor> getProcessingSteps() {

        List<Processor> processingSteps = new ArrayList<Processor>();
        for (int i = 0; i < 5; i++) {
            processingSteps.add(asynchProcessor);
        }
        return processingSteps.iterator();
    }
}