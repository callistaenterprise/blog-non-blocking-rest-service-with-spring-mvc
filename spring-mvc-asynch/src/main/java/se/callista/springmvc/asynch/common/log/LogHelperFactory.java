package se.callista.springmvc.asynch.common.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * Created by magnus on 30/05/14.
 */
@Component
public class LogHelperFactory {
    private @Autowired AutowireCapableBeanFactory beanFactory;

    @Value("${statistics.requestsPerLog}")
    private int STAT_REQS_PER_LOG;

    public LogHelper getLog(Class clz, String name) {
        return new LogHelper(clz, name, STAT_REQS_PER_LOG);
    }
}
