/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.convergentui;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.constants.ZuulConstants;
import com.netflix.zuul.context.RequestContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MimeType;

/**
 *
 * @author andrewserff
 */
public abstract class BaseResponseFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(BaseResponseFilter.class);

    private static DynamicBooleanProperty INCLUDE_DEBUG_HEADER = DynamicPropertyFactory
            .getInstance().getBooleanProperty(ZuulConstants.ZUUL_INCLUDE_DEBUG_HEADER,
                    false);

    private static DynamicIntProperty INITIAL_STREAM_BUFFER_SIZE = DynamicPropertyFactory
            .getInstance().getIntProperty(ZuulConstants.ZUUL_INITIAL_STREAM_BUFFER_SIZE,
                    1024);

    private static DynamicBooleanProperty SET_CONTENT_LENGTH = DynamicPropertyFactory
            .getInstance().getBooleanProperty(ZuulConstants.ZUUL_SET_CONTENT_LENGTH,
                    false);

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 1; //run before any others
    }

    @Override
    public boolean shouldFilter() {
        String contentType = getContentType(RequestContext.getCurrentContext());
        String verb = getVerb(RequestContext.getCurrentContext().getRequest());

        return "text/html".equals(contentType) && "GET".equalsIgnoreCase(verb) && (!RequestContext.getCurrentContext().getZuulResponseHeaders().isEmpty()
                || RequestContext.getCurrentContext().getResponseDataStream() != null
                || RequestContext.getCurrentContext().getResponseBody() != null);
    }

    private String getVerb(HttpServletRequest request) {
        String method = request.getMethod();
        if (method == null) {
            return "GET";
        }
        return method;
    }

    private String getContentType(RequestContext context) {
        List<Pair<String, String>> headers = context.getZuulResponseHeaders();
        String contentType = null;
        for (Pair<String, String> pair : headers) {
            if ("content-type".equalsIgnoreCase(pair.first())) {
                contentType = pair.second();
                break;
            }
        }
        
        MimeType type = MimeType.valueOf(contentType);
        if (type != null) {
            return type.getType() + "/" + type.getSubtype();
        } else {
            return contentType;
        }
    }

    protected void writeResponse(String responseBody) throws Exception {
        RequestContext context = RequestContext.getCurrentContext();
        // there is no body to send
        if (responseBody == null || responseBody.isEmpty()) {
            return;
        }
        HttpServletResponse servletResponse = context.getResponse();
        servletResponse.setCharacterEncoding("UTF-8");
        OutputStream outStream = servletResponse.getOutputStream();
        InputStream is = null;
        try {
            writeResponse(new ByteArrayInputStream(responseBody.getBytes()), outStream);
            return;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                outStream.flush();
                outStream.close();
            } catch (IOException ex) {
            }
        }
    }

    protected void writeResponse(InputStream zin, OutputStream out) throws Exception {
        byte[] bytes = new byte[INITIAL_STREAM_BUFFER_SIZE.get()];
        int bytesRead = -1;
        while ((bytesRead = zin.read(bytes)) != -1) {
            try {
                out.write(bytes, 0, bytesRead);
                out.flush();
            } catch (IOException ex) {
                // ignore
            }
            // doubles buffer size if previous read filled it
            if (bytesRead == bytes.length) {
                bytes = new byte[bytes.length * 2];
            }
        }
    }

    protected void addResponseHeaders() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletResponse servletResponse = context.getResponse();
        List<Pair<String, String>> zuulResponseHeaders = context.getZuulResponseHeaders();
        @SuppressWarnings("unchecked")
        List<String> rd = (List<String>) RequestContext.getCurrentContext().get(
                "routingDebug");
        if (rd != null) {
            StringBuilder debugHeader = new StringBuilder();
            for (String it : rd) {
                debugHeader.append("[[[" + it + "]]]");
            }
            if (INCLUDE_DEBUG_HEADER.get()) {
                servletResponse.addHeader("X-Zuul-Debug-Header", debugHeader.toString());
            }
        }
        if (zuulResponseHeaders != null) {
            for (Pair<String, String> it : zuulResponseHeaders) {
                servletResponse.addHeader(it.first(), it.second());
            }
        }
        RequestContext ctx = RequestContext.getCurrentContext();
        Integer contentLength = ctx.getOriginContentLength();
        // Only inserts Content-Length if origin provides it and origin response is not
        // gzipped
        if (SET_CONTENT_LENGTH.get()) {
            if (contentLength != null && !ctx.getResponseGZipped()) {
                servletResponse.setContentLength(contentLength);
            }
        }
    }
}
