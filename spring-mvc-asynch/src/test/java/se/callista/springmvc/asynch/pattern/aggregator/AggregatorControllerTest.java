package se.callista.springmvc.asynch.pattern.aggregator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import se.callista.springmvc.asynch.Application;
import se.callista.springmvc.asynch.common.AsynchTestBase;
import se.callista.springmvc.asynch.common.ProcessingStatus;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by magnus on 29/05/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class AggregatorControllerTest extends AsynchTestBase {

    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    @Value("${aggregator.timeoutMs}")
    private int TIMEOUT_MS;

    private final String expectedResultSingle = "{\"status\":\"Ok\",\"processingTimeMs\":2000}";
    private final String expectedResult =
        expectedResultSingle + '\n' +
        expectedResultSingle + '\n' +
        expectedResultSingle + '\n';

    @Before
    public void setup(){

        // Process mock annotations
        MockitoAnnotations.initMocks(this);

        // Setup Spring test in webapp-mode (same config as spring-boot)
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

    }

    @Test
    public void testAggregatorBlocking() throws Exception{
        this.mockMvc.perform(get("/aggregate-blocking?minMs=2000&maxMs=2000"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
            .andExpect(content().string(expectedResult));
    }

    @Test
    public void testAggregatorNonBlockingCallback() throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(get("/aggregate-non-blocking-callback?minMs=2000&maxMs=2000"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvcResult.getAsyncResult();

        this.mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
                .andExpect(content().string(expectedResult));
    }

    @Test
    public void testAggregatorNonBlockingCallbackTimeout() throws Exception {

        int minMs = (TIMEOUT_MS < 1000) ? 0 : TIMEOUT_MS - 1000;
        int maxMs = TIMEOUT_MS + 1000;
        int dbHits = 10;

        MvcResult mvcResult = this.mockMvc.perform(get("/aggregate-non-blocking-callback?dbHits=" + dbHits + "&minMs=" + minMs + "&maxMs=" + maxMs))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvcResult.getAsyncResult();

        this.mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=ISO-8859-1"));

        String result = mvcResult.getAsyncResult().toString();

        System.err.println("JSON: " + result);
        String[] psArr = result.split("\n");

        // Verify that we got some timeouts
        assertTrue("Expected at least one timeout to occur", psArr.length < dbHits);

        System.err.println("assert that no response time was over the timeout: " + TIMEOUT_MS);
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < psArr.length; i++) {
            ProcessingStatus ps = mapper.readValue(psArr[i], ProcessingStatus.class);
            System.err.println("psArr: " + ps.getStatus() + " - " + ps.getProcessingTimeMs());
            assertTrue(ps.getProcessingTimeMs() < TIMEOUT_MS);
        }
    }

    @Test
    public void testAggregatorNonBlockingLambda() throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(get("/aggregate-non-blocking-lambda?minMs=2000&maxMs=2000"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvcResult.getAsyncResult();

        this.mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
                .andExpect(content().string(expectedResult));
    }

    @Test
    public void testAggregatorNonBlockingLambdaTimeout() throws Exception {

        int minMs = (TIMEOUT_MS < 1000) ? 0 : TIMEOUT_MS - 1000;
        int maxMs = TIMEOUT_MS + 1000;
        int dbHits = 10;

        MvcResult mvcResult = this.mockMvc.perform(get("/aggregate-non-blocking-lambda?dbHits=" + dbHits + "&minMs=" + minMs + "&maxMs=" + maxMs))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvcResult.getAsyncResult();

        this.mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=ISO-8859-1"));

        String result = mvcResult.getAsyncResult().toString();

        System.err.println("JSON: " + result);
        String[] psArr = result.split("\n");

        // Verify that we got some timeouts
        assertTrue("Expected at least one timeout to occur", psArr.length < dbHits);

        System.err.println("assert that no response time was over the timeout: " + TIMEOUT_MS);
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < psArr.length; i++) {
            ProcessingStatus ps = mapper.readValue(psArr[i], ProcessingStatus.class);
            System.err.println("psArr: " + ps.getStatus() + " - " + ps.getProcessingTimeMs());
            assertTrue(ps.getProcessingTimeMs() < TIMEOUT_MS);
        }
    }
}