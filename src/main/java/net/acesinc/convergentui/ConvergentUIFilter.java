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
import net.acesinc.convergentui.content.ContentResponse;
import net.acesinc.convergentui.content.ContentService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A majority of the boilerplate code was stolen from the SendResponseFilter in
 * the spring-cloud-netflix project:
 * https://github.com/spring-cloud/spring-cloud-netflix/blob/master/spring-cloud-netflix-core/src/main/java/org/springframework/cloud/netflix/zuul/filters/post/SendResponseFilter.java
 *
 * @author andrewserff
 */
@Component
public class ConvergentUIFilter extends BaseResponseFilter {

    private static final Logger log = LoggerFactory.getLogger(ConvergentUIFilter.class);

    private static DynamicBooleanProperty INCLUDE_DEBUG_HEADER = DynamicPropertyFactory
            .getInstance().getBooleanProperty(ZuulConstants.ZUUL_INCLUDE_DEBUG_HEADER,
                    false);

    private static DynamicIntProperty INITIAL_STREAM_BUFFER_SIZE = DynamicPropertyFactory
            .getInstance().getIntProperty(ZuulConstants.ZUUL_INITIAL_STREAM_BUFFER_SIZE,
                    1024);

    private static DynamicBooleanProperty SET_CONTENT_LENGTH = DynamicPropertyFactory
            .getInstance().getBooleanProperty(ZuulConstants.ZUUL_SET_CONTENT_LENGTH,
                    false);

    @Autowired
    private ContentService contentManager;

    @Override
    public Object run() {

        String origBody = contentManager.getDownstreamResponse();
        if (origBody == null || origBody.isEmpty()) {
            return null;
        }
        
        String composedBody = null;
        log.trace("Response from downstream server: " + origBody);

        Document doc = Jsoup.parse(origBody);
        if (hasReplaceableElements(doc)) {
            log.debug("We have replaceable elements. Let's get em!");
            Elements elementsToUpdate = doc.select("div[data-loc]");
            for (Element e : elementsToUpdate) {
                String location = e.dataset().get("loc");
                String fragmentName = e.dataset().get("fragment-name");
                String cacheName = e.dataset().get("cache-name");
                boolean useCaching = !Boolean.valueOf(e.dataset().get("disable-caching"));
                boolean failQuietly = Boolean.valueOf(e.dataset().get("fail-quietly"));

                log.debug("Fetching content at location [ " + location + " ] with cacheName = [ " + cacheName + " ]");

                try {
                    RequestContext context = RequestContext.getCurrentContext();
                    ContentResponse response = contentManager.getContentFromService(location, cacheName, useCaching, context);

                    log.trace(response.toString());

                    StringBuilder content = new StringBuilder();
                    if (!response.isError()) {
                        String subContentResponse = response.getContent();
                        //TODO You better trust the source of your downstream HTML!
//                    String cleanedContent = Jsoup.clean(subContentResponse, Whitelist.basic()); //this totally stripped the html out...
                        Document subDocument = Jsoup.parse(subContentResponse);

                        if (fragmentName != null) {
                            Elements fragments = subDocument.select("div[data-fragment-name=\"" + fragmentName + "\"]");

                            if (fragments != null && fragments.size() > 0) {
                                if (fragments.size() == 1) {
                                    Element frag = fragments.first();
                                    content.append(frag.toString());

                                } else {
                                    for (Element frag : fragments) {
                                        content.append(frag.toString()).append("\n\n");
                                    }
                                }
                            } else {
                                log.debug("Found no matching fragments for [ " + fragmentName + " ]");
                                if (failQuietly) {
                                    content.append("<div class='cui-error'></div>");
                                } else {
                                    content.append("<span class='cui-error'>Failed getting content from remote service. Possible reason in reponse below</span>");
                                    content.append(subDocument.toString());
                                }
                            }
                        } else {
                            //take the whole thing and cram it in there!
                            content.append(subDocument.toString());
                        }
                    } else {
                        if (!failQuietly) {
                            content.append("<span class='cui-error'>Failed getting content from remote service. Reason: " + response.getMessage() + "</span>");
                        } else {
                            content.append("<div class='cui-error'></div>");
                        }
                    }

                    //now append it to the page
                    if (!content.toString().isEmpty()) {
                        e.html(content.toString());
                    }
                } catch (Throwable t) {
                    if (!failQuietly) {
                        e.html("<span class='cui-error'>Failed getting content from remote service. Reason: " + t.getMessage() + "</span>");
                    }
                    log.warn("Failed replacing content", t);
                }

            }

            composedBody = doc.toString();
        } else {
            log.debug("Document has no replaeable elements. Skipping");
        }

        try {
            addResponseHeaders();
            if (composedBody != null && !composedBody.isEmpty()) {
                writeResponse(composedBody);
            } else {
                writeResponse(origBody);
            }
        } catch (Exception ex) {
            log.error("Error sending response", ex);

        }
        return null;
    }

    protected boolean hasReplaceableElements(Document doc) {
        return doc.select("div[data-loc]").size() > 0;
    }
    
}
