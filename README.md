## Convergent UI

### License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

### Usage

To include Convergent UI into your Zuul Proxy, you need to do the following:

First, include the dependency in your pom.xml (or gradle.properities):

```
<dependency>
    <groupId>net.acesinc.util</groupId>
    <artifactId>security</artifactId>
    <version>1.0.0-SNAPSHOT</version>
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

And you're done!  Convergent UI will now scan all the HTML coming across your proxy for Convergent enabled documents.  


