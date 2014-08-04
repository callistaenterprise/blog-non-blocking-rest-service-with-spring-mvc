package se.callista.springmvc.asynch.pattern.routingslip.nonblocking.statemachine;

import com.ning.http.client.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.callista.springmvc.asynch.common.statemachine.Processor;
import se.callista.springmvc.asynch.common.statemachine.State;
import se.callista.springmvc.asynch.common.statemachine.StateMachine;

import java.io.IOException;

/**
 * Created by magnus on 15/05/14.
 */
@Component
public class AsynchProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(AsynchProcessor.class);

    @Autowired
    private StateMachine stateMachine;

    private static final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    @Override
    public void process(State state) {

        try {
            int sleeptimeMs = 100 * (state.getProcessingStepNo());
            String url = SP_NON_BLOCKING_URL + "?minMs=" + sleeptimeMs + "&maxMs=" + sleeptimeMs;

            LOG.debug("Launch asynch call");
            asyncHttpClient.prepareGet(url).execute(new AsynchProcessorCallback(stateMachine, state));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
