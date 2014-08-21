package se.callista.springmvc.asynch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import se.callista.springmvc.asynch.common.lambdasupport.AsyncHttpClientLambdaAware;
import se.callista.springmvc.asynch.config.MyEmbeddedServletContainerCustomizer;

@ComponentScan()
@EnableAutoConfiguration
public class Application {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    @Bean
    public EmbeddedServletContainerCustomizer embeddedServletCustomizer(){
        return new MyEmbeddedServletContainerCustomizer();
    }

    @Value("${threadPool.db.init_size}")
    private int THREAD_POOL_DB_INIT_SIZE;

    @Value("${threadPool.db.max_size}")
    private int THREAD_POOL_DB_MAX_SIZE;

    @Value("${threadPool.db.queue_size}")
    private int THREAD_POOL_DB_QUEUE_SIZE;

    @Bean(name="dbThreadPoolExecutor")
    public TaskExecutor getTaskExecutor() {
        ThreadPoolTaskExecutor tpte = new ThreadPoolTaskExecutor();
        tpte.setCorePoolSize(THREAD_POOL_DB_INIT_SIZE);
        tpte.setMaxPoolSize(THREAD_POOL_DB_MAX_SIZE);
        tpte.setQueueCapacity(THREAD_POOL_DB_QUEUE_SIZE);
        tpte.initialize();
        return tpte;
    }

    @Bean
    public AsyncHttpClientLambdaAware getAsyncHttpClient() {
        LOG.info("### Creates a new AsyncHttpClientLambdaAware-object");
        return new AsyncHttpClientLambdaAware();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}