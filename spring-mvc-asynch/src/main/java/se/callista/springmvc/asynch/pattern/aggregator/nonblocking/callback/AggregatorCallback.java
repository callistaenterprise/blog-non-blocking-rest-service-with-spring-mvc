package se.callista.springmvc.asynch.pattern.aggregator.nonblocking.callback;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;

public class AggregatorCallback extends AsyncCompletionHandler<Response> {

    private final int id;
    private final AggregatorEventHandler eventHandler;

    public AggregatorCallback(int id, AggregatorEventHandler eventHandler) {
        this.id = id;
        this.eventHandler = eventHandler;
    }

    @Override
    public Response onCompleted(Response response) throws Exception{
        eventHandler.onResult(id, response);
        return response;
    }

    @Override
    public void onThrowable(Throwable t){
        eventHandler.onError(id, t);
    }
}