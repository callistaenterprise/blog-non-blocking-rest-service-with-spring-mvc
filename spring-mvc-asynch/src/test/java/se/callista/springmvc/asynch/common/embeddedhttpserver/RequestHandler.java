package se.callista.springmvc.asynch.common.embeddedhttpserver;

import org.eclipse.jetty.server.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by magnus on 22/07/14.
 */
public interface RequestHandler {

    /** Handle a request.
     * @param target The target of the request - either a URI or a name.
     * @param baseRequest The original unwrapped request object.
     * @param request The request either as the {@link Request}
     * object or a wrapper of that request. The {@link org.eclipse.jetty.server.AbstractHttpConnection#getCurrentConnection()}
     * method can be used access the Request object if required.
     * @param response The response as the {@link org.eclipse.jetty.server.Response}
     * object or a wrapper of that request. The {@link org.eclipse.jetty.server.AbstractHttpConnection#getCurrentConnection()}
     * method can be used access the Response object if required.
     * @throws IOException
     * @throws ServletException
     */
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
