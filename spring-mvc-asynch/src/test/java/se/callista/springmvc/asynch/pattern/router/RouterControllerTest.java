package se.callista.springmvc.asynch.pattern.router;

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
import se.callista.springmvc.asynch.common.AsynchTestBase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Created by magnus on 29/05/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class RouterControllerTest extends AsynchTestBase {

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
    public void testRouterBlocking() throws Exception{
        String url = "/router-blocking?minMs=2000&maxMs=2000";
        this.mockMvc.perform(get(url))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
            .andExpect(content().string(expectedResult));
    }

    @Test
    public void testRouterNonBlockingCallback() throws Exception {

        String url = "/router-non-blocking-callback?minMs=2000&maxMs=2000";
        testNonBlocking(url);
    }

    @Test
    public void testRouterNonBlockingAnonymous() throws Exception {

        String url = "/router-non-blocking-anonymous?minMs=2000&maxMs=2000";
        testNonBlocking(url);
    }

    @Test
    public void testRouterNonBlockingLambda() throws Exception {

        String url = "/router-non-blocking-lambda?minMs=2000&maxMs=2000";
        testNonBlocking(url);
    }

    @Test
    public void testRouterNonBlockingSpring() throws Exception {

        // NOTE: Today's implementation in Spring of the AsyncRestTemplate is thread-blocking and therefore doesn't scale
        String url = "/router-non-blocking-anonymous?minMs=2000&maxMs=2000";
        testNonBlocking(url);
    }

    private void testNonBlocking(String url) throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(url))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvcResult.getAsyncResult();

        this.mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
                .andExpect(content().string(expectedResult));
    }


}