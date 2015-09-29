/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.util.test.service1.web;

import java.security.Principal;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author andrewserff
 */
@Controller
public class DontCacheController {

    private static final Logger log = LoggerFactory.getLogger(DontCacheController.class);

    @RequestMapping(value = {"/dontcache"}, method = RequestMethod.GET)
    public @ResponseBody
    String getPage(ModelMap model, Principal p) {
        model.addAttribute("pageName", "dontcache");
        return RandomStringUtils.randomAlphanumeric(15);
    }
}
