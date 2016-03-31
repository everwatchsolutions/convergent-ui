/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.convergentui;

import com.netflix.zuul.context.RequestContext;
import java.net.MalformedURLException;
import java.net.URL;
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
 * The ConvergentUIResponseFilter handles the html coming back from your main
 * service and will replace content with your template.
 *
 * @author andrewserff
 */
@Component
public class ConvergentUIResponseFilter extends BaseResponseFilter {

    private static final Logger log = LoggerFactory.getLogger(ConvergentUIResponseFilter.class);

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
                StringBuilder content = new StringBuilder();
                String location = e.dataset().get("loc");
                String fragmentName = e.dataset().get("fragment-name");
                String cacheName = e.dataset().get("cache-name");
                boolean useCaching = !Boolean.valueOf(e.dataset().get("disable-caching"));
                boolean failQuietly = Boolean.valueOf(e.dataset().get("fail-quietly"));
                boolean replaceOuter = e.dataset().get("replace-outer") == null ? true : Boolean.valueOf(e.dataset().get("replace-outer"));
                URL url = null;
                try {
                    url = new URL(location);
                    String protocol = url.getProtocol();
                    String service = url.getHost();

                    log.debug("Fetching content at location [ " + location + " ] with cacheName = [ " + cacheName + " ]");

                    try {
                        RequestContext context = RequestContext.getCurrentContext();
                        ContentResponse response = contentManager.getContentFromService(location, cacheName, useCaching, context);

                        log.trace(response.toString());

                        if (!response.isError()) {
                            Object resp = response.getContent();
                            if (String.class.isAssignableFrom(resp.getClass())) {
                                String subContentResponse = (String) resp;
                                //TODO You better trust the source of your downstream HTML!
//                    String cleanedContent = Jsoup.clean(subContentResponse, Whitelist.basic()); //this totally stripped the html out...
                                Document subDocument = Jsoup.parse(subContentResponse);

                                if (fragmentName != null) {
                                    Elements fragments = subDocument.select("div[data-fragment-name=\"" + fragmentName + "\"]");

                                    if (fragments != null && fragments.size() > 0) {
                                        if (fragments.size() == 1) {
                                            Element frag = fragments.first();

                                            //need to see if there are images that we need to replace the urls on
                                            Elements images = frag.select("img");
                                            for (Element i : images) {
                                                String src = i.attr("src");
                                                if (src.startsWith("/") && !src.startsWith("//")) {
                                                    i.attr("src", "/cui-req://" + protocol + "://" + service + src);
                                                } //else what do we do about relative urls?
                                            }

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
                                //not text...
                                if (!failQuietly) {
                                    content.append("<span class='cui-error'>Failed getting content from remote service. Reason: content was not text</span>");
                                } else {
                                    content.append("<div class='cui-error'></div>");
                                }
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
                    
                    if(replaceOuter) {
						// outer element should be replaced by content
						e.unwrap();
					}
                } catch (MalformedURLException ex) {
                    log.warn("location was invalid: [ " + location + " ]", ex);
                    if (!failQuietly) {
                        content.append("<span class='cui-error'>Failed getting content from remote service. Reason: data-loc was an invalid location.</span>");
                    } else {
                        content.append("<div class='cui-error'></div>");
                    }
                }

            }

            composedBody = doc.toString();
        } else {
            log.debug("Document has no replaeable elements. Skipping");
        }

        try {
            addResponseHeaders();
            if (composedBody != null && !composedBody.isEmpty()) {
                writeResponse(composedBody, getMimeType(RequestContext.getCurrentContext()));
            } else {
                writeResponse(origBody, getMimeType(RequestContext.getCurrentContext()));
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
