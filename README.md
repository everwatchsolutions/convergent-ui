## Convergent UI

Convergent UI is a special Zuul Filter that aims to provide a solution to the Distributed Composition problem faced when building a GUI within a Micro Services Architecture. Inspired by a [great article](https://medium.com/@clifcunn/nodeconf-eu-29dd3ed500ec) by [Clifton Cunningham on Medium](https://medium.com/@clifcunn) and his work on [Compoxure](https://github.com/tes/compoxure), we decided to work on porting their architecture to our Spring Cloud based architecture.  The Compoxure Architecture used many simular constructs available within Spring Cloud, so it made sense to us to port the ideas proposed by Clifton to the Spring framework.  

Distributed Composition is a term that describes the need for a single UI to include pieces of UIs from many services.  When building a Micro Services Architecture, we are told to seperate concerns as much as possible, and to us, that also means seperating the UIs from each other. At the same time, there is a desire to have a unified UI that doesn't look/act like a frameset from the 90's. Clifton provided a wondeful mockup of a UI that you might want to break up into smaller parts here:

![image](https://cdn-images-1.medium.com/max/800/1*YgK35pB22bXJm0LwqMz0Hw.jpeg)

As he described, each numbered section could come from different Micro Services on the backend. If you are interested in a more indepth description of the problem, different options for solving this problem, please read [Clifton's article](https://medium.com/@clifcunn/nodeconf-eu-29dd3ed500ec) or as [presented](http://dejanglozic.com/2014/10/20/micro-services-and-page-composition-problem/) by Dejan Glozic. 

To summarize Clifton's approach, he described a Proxy that would handle composing a single UI from multiple backend services which they implemented as Compoxure using many Node.js features along the way. The Spring Cloud Architecture, which relies on many of the Netflix Open Source projects, provides such a proxy called [Zuul](https://github.com/Netflix/zuul). The Zuul Edge Server provides a mechanism to inject Filters into the processing stream which is where we inject the `ConvergentUIFilter`. The Spring Framework and Spring Cloud also provides Circuit Breakers and Caching mechanisms that help us implement many of the other robust features described in the Compoxure architecture.  

Our approach is slightly different than the Compoxure approach, so read on to find out more about how Convergent UI works.  

### License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

### Usage

To include Convergent UI into your Zuul Proxy, you need to do the following:

First, include the dependency in your pom.xml (or gradle.properities):

```
<dependency>
  <groupId>net.acesinc</groupId>
  <artifactId>convergent-ui</artifactId>
  <version>1.0.4</version>
</dependency>
```
Next, in your application configuration, you need to tell Spring to scan our jar file for Spring Components.  To do that, you need to include the `@ComponentScan` annotation in your Application config like so:

```
@ComponentScan(basePackages = {"net.acesinc"})
```

In order for Caching to work, you need to provide a `CacheManager` implementation. We use Redis for our cache, so here is an example config:

```
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    @Bean
    public RedisTemplate<String, ContentResponse> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, ContentResponse> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(cf);
        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(RedisTemplate<String, ContentResponse> redisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);

        // Number of seconds before expiration. Defaults to unlimited (0)
        cacheManager.setDefaultExpiration(300);
        return cacheManager;
    }

}
```

As we've said, Convergent UI is implemented as a `ZuulFilter`, so you will obviously need to be running Zuul in your architecture. In Compoxure, you had to specify where the base HTML layout would come from in their configuration. Our configuration is externalized and seperated from Convergent UI. All you need to do is set up your routing in the Zuul Edge Server and then Convergent UI will process HTML that comes from those routing endpoints. In our setup, our Zuul Proxy routes traffic to a common UI micro service. This common UI micro service serves up a HTML layout that defines the over all page and can now point to and include HTML and other content from other back end micro services as well.  

So, once you have setup Zuul and installed the Convergent UI Filter, you are ready to go! Convergent UI will now scan all the HTML coming across your proxy for Convergent enabled documents.  

### Convergent UI Syntax

The ConvergentUIFilter scans HTML coming across the Proxy for content enriched with some special tags.  The most important of those tags is the `data-loc` tag.  The `data-loc` tag specifies the location of the content you wish to replace a section of your composable page with. We also took a page from the [ThymeLeaf](http://www.thymeleaf.org/) book and introduced the idea of a Fragment within a page that you can include.  This allows you to support both a stand alone UI in your microservice and also include the important parts of that UI in your Converged UI.  

| Property        | Description   |
| --------------- |----------------|
| `data-loc` | Defines the location of the remote content to include.  The location specified should be a service registered with the Eureka Discovery Service.  Example: http://my-service/content1 |
| `data-cache-name` | A unique name for this section of the content. Will be used in the local cache key |
| `data-fragment-name` | A unique name of a fragment of the content provided by the data-loc. This allows you to request an entire HTML page and only include a section of that page that contains a `data-fragment-name` that matches this name.  |
| `data-fail-quietly` | If true and a failure occurs, the content section will be replaced with an empty `<div class='cui-error'>`. If false, the content section will be replaced with an error message inside a `<span class='cui-error'></span>` |
| `data-disable-caching` | If you would like to disable caching for this location, set this to true |

An example section of HTML follows:


```
	<div data-loc="https://service2/test2" data-fail-quietly="false" data-fragment-name="test1" data-cache-name="service2:test2">
    	replace me!
	</div>
```

The content of that div will be replaced with the html contained at https://service2/test2 and the fragment with a name of test1.  i.e. if the response from https://service2/test2 was:

```
	<!DOCTYPE html>
	<!--
	To change this license header, choose License Headers in Project Properties.
	To change this template file, choose Tools | Templates
	and open the template in the editor.
	-->
	<html>
	    <head>
        	<title>Test 2</title>
	        <meta charset="UTF-8"/>
   	    	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    	</head>
	    <body>
    	    <div>This is test page 2</div>
        	<div data-fragment-name='test1'>
            	This is content from Service 2!
	        </div>
    	</body>
	</html>
```
The `<div>` would look like the following after passing through the ConvergentUIFilter:

```
	<div data-loc="https://service2/test2" data-fail-quietly="false" data-fragment-name="test1" data-cache-name="service2:test2">	
		<div data-fragment-name='test1'>
    		This is content from Service 2!
		</div>
	</div>
```

#### Images, CSS & Javascript

Convergent-UI also supports the ability to request images, javascript, css, json, etc from the backend services. This is useful because the HTML that might be served from the backend service could contain images.  It could also require some special styling or javascript in order to look/work correctly.  Convergent-UI will scrape the HTML that is returned from backend services for any image tags whose `src` start with a '/' (meaning they are trying to load images from the root of the context (i.e. /images/image.png)). If it find any, it will replace the `src` with a special URL that will act as a hint to Convergent-UI to pass that request through to the backend service that the HTML came from.  This special URL format can also be used to retrieve css/javascript from the backend service as well.  The format is as follows:

```
/cui-req://http://service-name/path/to/resource
```

As an example, if you wanted to load the javascript for a page served from the backend service `service1` you would do the following:

```
 <script src="/cui-req://http://service1/js/app.js"></script>
```

This will force the page to request the script though the Proxy which enacts the ConvergentUIRequestFilter who gets the data and returns it to you. 

You should be aware that any content in your backend service can be exposed via this manner, however, only GET requests are forwarded. Therefore, it's recommended that you protect your resources as needed to ensure you are only exposing the resources you want exposed to prying eyes. 


### Example

If you want to see ConvergentUI in action, check out the [`example` directory](https://github.com/acesinc/convergent-ui/tree/master/example) and run the example on your own box to see how it works.  

