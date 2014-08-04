package se.callista.springmvc.asynch.common.lambdasupport;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

import java.io.IOException;

/**
 * Created by magnus on 18/07/14.
 */
public class AsyncHttpClientLambdaAware {

    private static final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public ListenableFuture<Response> execute(String url, final Completed c, final Error e) throws IOException {
        return asyncHttpClient.prepareGet(url).execute(new AsyncCompletionHandler<Response>() {

            @Override
            public Response onCompleted(Response response) throws Exception {
                return c.onCompleted(response);
            }

            @Override
            public void onThrowable(Throwable t) {
                e.onThrowable(t);
            }

        });
    };

    public ListenableFuture<Response> execute(String url, final Completed c) throws IOException {
        return asyncHttpClient.prepareGet(url).execute(new AsyncCompletionHandler<Response>() {

            @Override
            public Response onCompleted(Response response) throws Exception {
                return c.onCompleted(response);
            }
        });
    };
}