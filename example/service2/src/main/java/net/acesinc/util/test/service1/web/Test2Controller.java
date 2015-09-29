/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.acesinc.util.test.service1.web;

import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author andrewserff
 */
@Controller
public class Test2Controller {
    private static final Logger log = LoggerFactory.getLogger(Test2Controller.class);
    
    @RequestMapping(value = {"/test2"}, method = RequestMethod.GET)
    public String getPage(ModelMap model, Principal p) {
        model.addAttribute("pageName", "test2");
        return "test2";
    }
}
