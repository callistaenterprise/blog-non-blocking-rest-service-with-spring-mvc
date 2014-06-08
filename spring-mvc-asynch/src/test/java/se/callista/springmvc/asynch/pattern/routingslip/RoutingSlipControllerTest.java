package se.callista.springmvc.asynch.pattern.routingslip;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

/**
 * Created by magnus on 29/05/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class RoutingSlipControllerTest {

    @Autowired
    RoutingSlipController customerController;

    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    private final String expectedResult =
        "{\"status\":\"Ok\",\"processingTimeMs\":100}\n" +
        "{\"status\":\"Ok\",\"processingTimeMs\":200}\n" +
        "{\"status\":\"Ok\",\"processingTimeMs\":300}\n" +
        "{\"status\":\"Ok\",\"processingTimeMs\":400}\n" +
        "{\"status\":\"Ok\",\"processingTimeMs\":500}\n";

    @Before
    public void setup(){

        // Process mock annotations
        MockitoAnnotations.initMocks(this);

        // Setup Spring test in webapp-mode (same config as spring-boot)
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testRoutingSlipBlocking() throws Exception{
        this.mockMvc.perform(get("/routing-slip-blocking"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
            .andExpect(content().string(expectedResult));
    }

    @Test
    public void testRoutingSlipNonBlocking() throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(get("/routing-slip-non-blocking"))
            .andExpect(request().asyncStarted())
//            .andExpect(request().asyncResult(expectedResult))
            .andReturn();

        mvcResult.getAsyncResult();

        this.mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
            .andExpect(content().string(expectedResult));
    }
}