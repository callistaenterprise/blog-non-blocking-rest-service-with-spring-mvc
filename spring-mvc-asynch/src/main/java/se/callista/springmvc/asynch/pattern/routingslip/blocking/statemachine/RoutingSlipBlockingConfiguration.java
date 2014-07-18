package se.callista.springmvc.asynch.pattern.routingslip.blocking.statemachine;

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
public class RoutingSlipBlockingConfiguration {

    @Autowired
    private SynchProcessor synchProcessor;

    /**
     * Simulates setting up a number of processing steps from some kind of configuration...
     *
     * @return
     */
    public Iterator<Processor> getProcessingSteps() {

        List<Processor> processingSteps = new ArrayList<Processor>();
        for (int i = 0; i < 5; i++) {
            processingSteps.add(synchProcessor);
        }
        return processingSteps.iterator();
    }
}
