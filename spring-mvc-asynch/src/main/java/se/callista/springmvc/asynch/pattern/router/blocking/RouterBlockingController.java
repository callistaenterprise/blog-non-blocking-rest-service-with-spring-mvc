package se.callista.springmvc.asynch.pattern.router.blocking;

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
public class RouterBlockingController {

    private LogHelper LOG;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private LogHelperFactory logFactory;

    @Value("${sp.non_blocking.url}")
    private String SP_NON_BLOCKING_URL;

    @PostConstruct
    public void initAfterInject() {
        LOG = logFactory.getLog(RouterBlockingController.class, "router");
    }

    /**
     * Sample usage: curl "http://localhost:9080/router-blocking?minMs=1000&maxMs=2000"
     *
     * @param minMs
     * @param maxMs
     * @return
     */
    @RequestMapping("/router-blocking")
    public String blockingRouter(
        @RequestParam(value = "minMs", required = false, defaultValue = "0") int minMs,
        @RequestParam(value = "maxMs", required = false, defaultValue = "0") int maxMs) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        LOG.logStartBlocking();

        try {
            String url = SP_NON_BLOCKING_URL + "?minMs={minMs}&maxMs={maxMs}";
            ResponseEntity<String> result = restTemplate.getForEntity(url, String.class, minMs, maxMs);
    
            // TODO: Handle status codes other than 200...
            status = result.getStatusCode();

            return result.getBody();

        } finally {
            LOG.logEndBlocking(status.value());
        }
    }
}