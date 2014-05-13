package se.callista.springmvc.asynch.aggregator;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;

public class AggregatorCallback extends AsyncCompletionHandler<Response> {

    private final AggregatorEventHandler eventHandler;

    public AggregatorCallback(AggregatorEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public Response onCompleted(Response response) throws Exception{
        eventHandler.onResult(response);
        return response;
    }

    @Override
    public void onThrowable(Throwable t){
        eventHandler.onError(t);
    }
}