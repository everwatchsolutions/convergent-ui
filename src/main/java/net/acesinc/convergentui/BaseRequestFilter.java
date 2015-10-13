/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.convergentui;

import com.netflix.zuul.context.RequestContext;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author andrewserff
 */
public abstract class BaseRequestFilter extends BaseFilter  {

    private static final Logger log = LoggerFactory.getLogger(BaseRequestFilter.class);
    
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    /**
     * Only filter on GET requests and if the requests conform to the ConvergentUI request format
     * @return true if we should filter
     */
    @Override
    public boolean shouldFilter() {
        HttpServletRequest req = RequestContext.getCurrentContext().getRequest();
        return "GET".equalsIgnoreCase(getVerb(req)) && isConvergentUIRequest(req);
    }
}
