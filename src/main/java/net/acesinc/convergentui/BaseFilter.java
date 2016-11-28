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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

/**
 * A majority of the boilerplate code was stolen from the SendResponseFilter in
 * the spring-cloud-netflix project:
 * https://github.com/spring-cloud/spring-cloud-netflix/blob/master/spring-cloud-netflix-core/src/main/java/org/springframework/cloud/netflix/zuul/filters/post/SendResponseFilter.java
 *
 * @author andrewserff
 */
public abstract class BaseFilter extends ZuulFilter {

    private static DynamicBooleanProperty INCLUDE_DEBUG_HEADER = DynamicPropertyFactory
            .getInstance().getBooleanProperty(ZuulConstants.ZUUL_INCLUDE_DEBUG_HEADER,
                    false);

    private static DynamicIntProperty INITIAL_STREAM_BUFFER_SIZE = DynamicPropertyFactory
            .getInstance().getIntProperty(ZuulConstants.ZUUL_INITIAL_STREAM_BUFFER_SIZE,
                    1024);

    private static DynamicBooleanProperty SET_CONTENT_LENGTH = DynamicPropertyFactory
            .getInstance().getBooleanProperty(ZuulConstants.ZUUL_SET_CONTENT_LENGTH,
                    false);

    protected boolean isConvergentUIRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/cui-req://");
    }

    protected String getVerb(HttpServletRequest request) {
    	String method = "GET";
    	if (request != null && request.getMethod() != null) {
    		method = request.getMethod();
    	}
        return method;
    }

    protected MimeType getMimeType(RequestContext context) {
        List<Pair<String, String>> headers = context.getZuulResponseHeaders();
        String contentType = null;
        for (Pair<String, String> pair : headers) {
            if ("content-type".equalsIgnoreCase(pair.first())) {
                contentType = pair.second();
                break;
            }
        }

        if (contentType != null) {
            MimeType type = MimeType.valueOf(contentType);
            return type;
        }
        return null;
    }

    protected String getContentType(RequestContext context) {
        String contentType = "unknown";
        MimeType type = getMimeType(context);
        if (type != null) {
            contentType = type.getType() + "/" + type.getSubtype();
        }
        
        return contentType;
    }

    protected void writeResponse(String responseBody, MimeType contentType) throws Exception {
        RequestContext context = RequestContext.getCurrentContext();
        // there is no body to send
        if (responseBody == null || responseBody.isEmpty()) {
            return;
        }
        HttpServletResponse servletResponse = context.getResponse();
        servletResponse.setCharacterEncoding("UTF-8");
        servletResponse.setContentType(contentType.toString());
        OutputStream outStream = servletResponse.getOutputStream();
        InputStream is = null;
        try {
            writeResponse(new ByteArrayInputStream(responseBody.getBytes()), outStream);
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

    protected void writeResponse(BufferedImage image, MediaType type) throws Exception {
        RequestContext context = RequestContext.getCurrentContext();
        // there is no body to send
        if (image == null) {
            return;
        }
        HttpServletResponse servletResponse = context.getResponse();
//        servletResponse.setCharacterEncoding("UTF-8");
        servletResponse.setContentType(type.toString());
        
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        ImageIO.write(image, type.getSubtype(), tmp);
        tmp.close();
        Integer contentLength = tmp.size();
        
        servletResponse.setContentLength(contentLength);
        
        OutputStream outStream = servletResponse.getOutputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ImageIO.write(image, type.getSubtype(), os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());

        try {
            writeResponse(is, outStream);
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
        Long contentLength = ctx.getOriginContentLength();
        // Only inserts Content-Length if origin provides it and origin response is not
        // gzipped
        if (SET_CONTENT_LENGTH.get()) {
            if (contentLength != null && !ctx.getResponseGZipped()) {
                servletResponse.setContentLengthLong(contentLength);
            }
        }
    }
}
