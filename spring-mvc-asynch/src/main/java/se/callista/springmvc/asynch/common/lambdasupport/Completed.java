package se.callista.springmvc.asynch.common.lambdasupport;

import com.ning.http.client.Response;

/**
 * Created by magnus on 18/07/14.
 */
public interface Completed {
    public Response onCompleted(Response response) throws Exception;
}
