package se.callista.springmvc.asynch.common.embeddedhttpserver;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Created by magnus on 22/07/14.
 */
abstract public class EmbeddedHttpServerTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedHttpServerTestBase.class);

    private EmbeddedHttpServer server;

    @Value("${sp.server.port}")
    private int port;

    @Before
    public void startTestServer() throws Exception {
        LOG.debug("Start embedded Jetty-server on port: " + port);
        server = new EmbeddedHttpServer(port, (target, baseRequest, request, response) -> {
            String requestBody = IOUtils.toString(baseRequest.getInputStream());
            createResponse(request, requestBody, response);
            baseRequest.setHandled(true);
        });
        server.start();
    }

    @After
    public void stopTestServer() throws Exception {
        server.stop();
    }

    abstract protected void createResponse(HttpServletRequest request, String requestBody, HttpServletResponse response) throws IOException, URISyntaxException;

    protected void getQueryParameters(HttpServletRequest request, Map<String, String> parMap) throws URISyntaxException {
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(request.getRequestURL().toString() + "?" + request.getQueryString()), "UTF-8");

        for (NameValuePair param : params) {
            parMap.put(param.getName(), param.getValue());
        }
    }
}
