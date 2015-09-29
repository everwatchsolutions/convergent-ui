/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.util.test.service1.config;

import java.util.Arrays;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

/**
 *
 * @author andrewserff
 */
@Configuration
@ComponentScan(basePackages = "net.acesinc")
public class MVCConfig extends WebMvcConfigurationSupport {

    private Logger log = LoggerFactory.getLogger(MVCConfig.class);

    @Autowired
    private Environment env;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("/static/");
    }

    @Bean
    public ThymeleafViewResolver thymeleafViewResolver(WebApplicationContext wac) {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCharacterEncoding("UTF-8");
        templateEngine.setTemplateResolver(templateResolver);
        resolver.setTemplateEngine(templateEngine);
        resolver.setOrder(2);
        resolver.setApplicationContext(wac);
        resolver.setCharacterEncoding("UTF-8");

        //Enable the Spring Security Thymeleaf integration
        templateEngine.addDialect(new LayoutDialect());

        // caching
        String[] activeProfiles = env.getActiveProfiles();
        if (Arrays.asList(activeProfiles).contains("dev")) {
            log.info("DEV Profile is active. Disabling template caching.");
            templateResolver.setCacheable(false);
            templateEngine.setCacheManager(null);
            resolver.setCache(false);
        }
        return resolver;
    }
}
