package se.callista.springmvc.asynch.teststub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@EnableAutoConfiguration
public class Application {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    @Bean
    public EmbeddedServletContainerCustomizer embeddedServletCustomizer(){
        return new MyEmbeddedServletContainerCustomizer();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}