/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.util;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;

/**
 *
 * @author andrewserff
 */
@SpringBootApplication
@Controller
@EnableAutoConfiguration
@EnableZuulProxy
@ComponentScan(basePackages = {"net.acesinc"})
public class ZuulEdgeServer {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ZuulEdgeServer.class).web(true).run(args);
    }
}
