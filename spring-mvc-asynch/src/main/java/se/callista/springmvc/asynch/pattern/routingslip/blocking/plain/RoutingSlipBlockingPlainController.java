package se.callista.springmvc.asynch.pattern.routingslip.blocking.plain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import se.callista.springmvc.asynch.common.log.LogHelper;
import se.callista.springmvc.asynch.common.log.LogHelperFactory;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@RestController
public class RoutingSlipBlockingPlainController {

    private LogHelper LOG;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private LogHelperFactory logFactory;

    @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    List<String> resultList = new ArrayList<>();

    @PostConstruct
    public void initAfterInject() {
        LOG = logFactory.getLog(RoutingSlipBlockingPlainController.class, "routing-slip");
    }

    /**
     * Sample usage: curl "http://localhost:9080/routing-slip-blocking-plain"
     *
     * @return
     */
    @RequestMapping("/routing-slip-blocking-plain")
    public String blockingRouter() {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        LOG.logStartBlocking();

        // Send request #1 and process its result
        String result = executeProcessingStep(1);
        processResult(result);

        // Send request #2 and process its result
        result = executeProcessingStep(2);
        processResult(result);

        // Send request #3 and process its result
        result = executeProcessingStep(3);
        processResult(result);

        // Send request #4 and process its result
        result = executeProcessingStep(4);
        processResult(result);

        // Send request #5 and process its result
        result = executeProcessingStep(5);
        processResult(result);

        LOG.logEndBlocking(HttpStatus.OK.value());

        return getTotalResult();
    }

    private String executeProcessingStep(int processingStepNo) {
        LOG.logStartProcessingStepNonBlocking(processingStepNo);
        ResponseEntity<String> result = restTemplate.getForEntity(getUrl(processingStepNo), String.class);
        LOG.logEndProcessingStepNonBlocking(processingStepNo, result.getStatusCode().value());
        return result.getBody();
    }

    private String getUrl(int processingStepNo) {
        int sleeptimeMs = 100 * processingStepNo;
        return SP_NON_BLOCKING_URL + "?minMs=" + sleeptimeMs + "&maxMs=" + sleeptimeMs;
    }

    private void processResult(String result) {
        resultList.add(result);
    }

    private String getTotalResult() {
        String totalResult = "";
        for (String r : resultList)
            totalResult += r + '\n';
        return totalResult;
    }

}