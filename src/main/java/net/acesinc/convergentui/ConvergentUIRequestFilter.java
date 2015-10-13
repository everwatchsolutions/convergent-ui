/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.convergentui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.context.RequestContext;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.acesinc.convergentui.content.ContentResponse;
import net.acesinc.convergentui.content.ContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

/**
 * The ConvergentUIRequestFilter handles special requests to get data such as
 * images, javascript, css, etc from downstream services. ConvergentUI Requests
 * must conform to a special format that looks like: /cui-req:// An example of
 * how to use this might be:
 * <img src="/cui-req://service2/images/img.png" />
 * This will request the image "img.png" from service2 at the path
 * /images/img.png
 *
 * @author andrewserff
 */
@Component
public class ConvergentUIRequestFilter extends BaseRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ConvergentUIRequestFilter.class);

    @Autowired
    private ContentService contentManager;
    private ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public Object run() {
        //First we need to build the correct URL
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest req = ctx.getRequest();

        String path = req.getRequestURI();

        String location = path.substring("/cui-req://".length());
        log.debug("RequestFilter for [ " + location + " ] in process");

        RequestContext context = RequestContext.getCurrentContext();
        ContentResponse response = contentManager.getContentFromService(location, location, false, context);

        MimeType type = response.getContentType();
        
        addResponseHeaders();

        if (!response.isError()) {
            Object resp = response.getContent();
            try {
                
                if (String.class.isAssignableFrom(resp.getClass())) {
                    writeResponse((String) resp, type);
                } else if (BufferedImage.class.isAssignableFrom(resp.getClass())) {
                    writeResponse((BufferedImage) resp, response.getContentType());
                } else if (/*Map.class.isAssignableFrom(resp.getClass()) &&*/ type.getSubtype().contains("json")) {
                    writeResponse(mapper.writeValueAsString(resp), type);
                } else {
                    
                    log.warn("Unknown response type [ " + response.getContentType() + " ] that we can't handle yet. Content is of type: " + resp.getClass());
                }
            } catch (Exception ex) {
                log.error("Error writing response", ex);
            }
        }
        return null;

    }

}
