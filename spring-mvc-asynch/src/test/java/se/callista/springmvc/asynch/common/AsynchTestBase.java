package se.callista.springmvc.asynch.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.callista.springmvc.asynch.common.embeddedhttpserver.EmbeddedHttpServerTestBase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.io.IOUtils.write;

/**
 * Created by magnus on 22/07/14.
 */
abstract public class AsynchTestBase extends EmbeddedHttpServerTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(AsynchTestBase.class);

    protected void createResponse(HttpServletRequest request, String requestBody, HttpServletResponse response) throws IOException, URISyntaxException {

        Map<String, String> parMap = new HashMap<>();
        parMap.put("minMs", "0");
        parMap.put("maxMs", "0");
        getQueryParameters(request, parMap);

        int minMs = Integer.parseInt(parMap.get("minMs"));
        int maxMs = Integer.parseInt(parMap.get("maxMs"));
        int processingTimeMs = calculateProcessingTime(minMs, maxMs);

        LOG.debug("Start blocking request, processing time: {} ms (" + minMs + " - " + maxMs + ").", processingTimeMs);
        try {
            Thread.sleep(processingTimeMs);
        }
        catch (InterruptedException e) {}

        String responseBody = "{\"status\":\"Ok\",\"processingTimeMs\":" + processingTimeMs + "}";
        response.setStatus(SC_OK);
        response.setContentType("text/plain;charset=ISO-8859-1");
        write(responseBody, response.getOutputStream());
    }

    private int calculateProcessingTime(int minMs, int maxMs) {
        if (maxMs < minMs) maxMs = minMs;
        int processingTimeMs = minMs + (int) (Math.random() * (maxMs - minMs));
        return processingTimeMs;
    }
}