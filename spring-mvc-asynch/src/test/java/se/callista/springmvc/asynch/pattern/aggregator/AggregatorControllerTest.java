package se.callista.springmvc.asynch.pattern.aggregator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import se.callista.springmvc.asynch.Application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by magnus on 29/05/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class AggregatorControllerTest {

    @Autowired
    AggregatorController customerController;

    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    private final String expectedResult = "{\"status\":\"Ok\",\"processingTimeMs\":2000}";

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
    public void testAggregatorNonBlocking() throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(get("/aggregate-non-blocking?minMs=2000&maxMs=2000"))
            .andExpect(request().asyncStarted())
            .andReturn();

        mvcResult.getAsyncResult();

        this.mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
            .andExpect(content().string(expectedResult));
    }
}