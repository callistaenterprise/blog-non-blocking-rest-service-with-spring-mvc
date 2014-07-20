package se.callista.springmvc.asynch.pattern.aggregator.blocking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import se.callista.springmvc.asynch.common.log.LogHelper;
import se.callista.springmvc.asynch.common.log.LogHelperFactory;

import javax.annotation.PostConstruct;

@RestController
public class AggregatorBlockingController {

    private static LogHelper LOG;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private LogHelperFactory logFactory;

    @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    @PostConstruct
    public void initAfterInject() {
        LOG = logFactory.getLog(AggregatorBlockingController.class, "aggregator");
    }

    /**
     * Sample usage: curl "http://localhost:9080/aggregate-blocking?minMs=1000&maxMs=2000"
     *
     * @param dbLookupMs
     * @param dbHits
     * @param minMs
     * @param maxMs
     * @return
     */
    @RequestMapping("/aggregate-blocking")
    public String blockingAggregator(
        @RequestParam(value = "dbLookupMs", required = false, defaultValue = "200") int dbLookupMs,
        @RequestParam(value = "dbHits",     required = false, defaultValue = "3")   int dbHits,
        @RequestParam(value = "minMs",      required = false, defaultValue = "0")   int minMs,
        @RequestParam(value = "maxMs",      required = false, defaultValue = "0")   int maxMs) {

        DbLookup dbLookup = new DbLookup(LOG, dbLookupMs, dbHits);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        LOG.logStartBlocking();

        String aggregatedResult = "";
        try {
            int noOfCalls = dbLookup.executeDbLookup();

            for (int i = 0; i < noOfCalls; i++) {
                LOG.logStartProcessingStepBlocking(i);
                ResponseEntity<String> result = restTemplate.getForEntity(
                    SP_NON_BLOCKING_URL + "?minMs={minMs}&maxMs={maxMs}", String.class, minMs, maxMs);

                // TODO: Handle status codes other than 200...
                status = result.getStatusCode();
                LOG.logEndProcessingStepBlocking(i, status.value());

                aggregatedResult += result.getBody() + '\n';
            }

            return aggregatedResult;

        } finally {
            LOG.logEndBlocking(status.value());
        }
    }
}