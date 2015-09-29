/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author andrewserff
 */
public class JSoupTest {

    private static final Logger log = LoggerFactory.getLogger(JSoupTest.class);

    public JSoupTest() {
        String html = "<!DOCTYPE html>\n"
                + "\n"
                + "<!--\n"
                + "To change this license header, choose License Headers in Project Properties.\n"
                + "To change this template file, choose Tools | Templates\n"
                + "and open the template in the editor.\n"
                + "-->\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <title>Test 2</title>\n"
                + "        <meta charset=\"UTF-8\" />\n"
                + "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <div>This is test page 2</div>\n"
                + "        <div data-fragment-name=\"test1\">\n"
                + "            This is content from Service 2!\n"
                + "        </div>\n"
                + "    </body>\n"
                + "</html>";

        log.info("HTML Before Clean: " + html);
        String cleanedContent = Jsoup.clean(html, Whitelist.relaxed());
        log.info("HTML After Clean: " + cleanedContent);
        Document subDocument = Jsoup.parse(html);
        log.info("Doc: " + subDocument.html());
        Elements fragments = subDocument.select("div[data-fragment-name=\"test1\"]");
        if (fragments != null && fragments.size() > 0) {
            log.info("Found " + fragments.size() + " elements");
            for (Element e : fragments) {
                log.info("Element: " + e.html());
            }
        } else {
            log.warn("Didn't find any matching elements");
        }
    }

    public static void main(String[] args) {
        JSoupTest test = new JSoupTest();
    }
}
