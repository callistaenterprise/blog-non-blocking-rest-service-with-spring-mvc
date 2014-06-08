package se.callista.springmvc.asynch.common.deferredresult;

import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CountDownLatch;

/**
 * Created by magnus on 16/05/14.
 */
public class DeferredResultWithBlockingWait<T> extends DeferredResult<T> {
    final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public boolean setResult(T result) {
        latch.countDown();
        return super.setResult(result);
    }

    public void await() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
