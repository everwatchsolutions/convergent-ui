/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.convergentui;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.zuul.constants.ZuulConstants;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author andrewserff
 */
public abstract class BaseResponseFilter extends BaseFilter {

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
        RequestContext ctx = RequestContext.getCurrentContext();
        String contentType = getContentType(ctx);
        String verb = getVerb(ctx.getRequest());

        return "text/html".equals(contentType) && "GET".equalsIgnoreCase(verb) && 
                (!ctx.getZuulResponseHeaders().isEmpty()
                || ctx.getResponseDataStream() != null
                || ctx.getResponseBody() != null);
    }
}
