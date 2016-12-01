/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.convergentui.content;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.zuul.context.RequestContext;
import java.awt.image.BufferedImage;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author andrewserff
 */
public class ContentFetchCommand extends HystrixCommand<ContentResponse> {

    private static final Logger log = LoggerFactory.getLogger(ContentFetchCommand.class);
    private RestTemplate restTemplate;
    private ProxyRequestHelper helper;

    private String location;
    private RequestContext requestContext;

    public ContentFetchCommand(String location, RequestContext requestContext, RestTemplate restTemplate) {
        super(HystrixCommandGroupKey.Factory.asKey("content-fetch"));
        this.location = location;
        this.requestContext = requestContext;
        this.restTemplate = restTemplate;
        helper = new ProxyRequestHelper();
    }

    @Override
    protected ContentResponse run() throws Exception {
        log.debug("Getting live content from [ " + location + " ]");
        try {
            HttpServletRequest request = requestContext.getRequest();
            MultiValueMap<String, String> headers = this.helper
                    .buildZuulRequestHeaders(request);

            if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
                MultiValueMap<String, String> params = this.helper
                        .buildZuulRequestQueryParams(request);
            }

            HttpHeaders requestHeaders = new HttpHeaders();
            for (String key : headers.keySet()) {
                for (String s : headers.get(key)) {
                    requestHeaders.add(key, s);
                }
            }
            HttpEntity requestEntity = new HttpEntity(null, requestHeaders);

            ResponseEntity<Object> exchange = this.restTemplate.exchange(location, HttpMethod.GET, requestEntity, Object.class);
            
            ContentResponse response = new ContentResponse();
            response.setContent(exchange.getBody());
            response.setContentType(exchange.getHeaders().getContentType());
            response.setError(false);

            return response;
        } catch (Exception e) {
            log.debug("Error fetching live content from [ " + location + " ]", e);
            throw e;
        }
    }

    @Override
    protected ContentResponse getFallback() {
        log.debug("ContentFetch failed for [ " + location + " ]. Returing fallback response");
        ContentResponse response = new ContentResponse();
        response.setContent("");
        response.setError(true);
        
        String defaultErrorMsg = getFailedExecutionException().getMessage();
        if (defaultErrorMsg == null || defaultErrorMsg.isEmpty()) {
            defaultErrorMsg = "Unknown Error";
        }
        //see if we can get a better error message
        Exception errorFromThrowable = getExceptionFromThrowable(getExecutionException());
        String errMessage = (errorFromThrowable != null) ? errorFromThrowable.getMessage() : defaultErrorMsg;
        
        response.setMessage(errMessage);
        return response;
    }
}
