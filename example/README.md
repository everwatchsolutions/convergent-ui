# Convergent UI Example 
In order to show you how Convergent UI works, we have provided a set of example micro services running with the Spring Cloud infrastructure. You will need to run each of the services to show how everything works.  Below is a basic overview of how to start the entire example.  

## Running

This micro services architecture relies on some of the Spring Cloud intrastructure to work.  So you need to run the Discovery Server and the Edge Server in order for anything to work.  Once you have those running, start the two other example services (`service1` & `service2`). Note, you don't have to start them in this order, it just make more sense to run them in that order.  

### Dependencies
Note that you will need a Redis service up and running on localhost at the default port 6379 before you begin. You can pretty easily get this up and running on Mac OS X by doing:
```
brew install redis
redis-server /usr/local/etc/redis.conf
```

### Discovery Server
 
 ```
 cd discovery-server
 mvn clean spring-boot:run
 ```
 
### Edge Server
 
 ```
 cd edge-server
 mvn clean spring-boot:run
 ``` 
 
### Service1

 ```
 cd service1
 mvn clean spring-boot:run
 ```
 
### Service2

 ```
 cd service2
 mvn clean spring-boot:run
 ```
## Testing it out

Now that you have everything running, you can load the UI @ [`http://localhost:8080`](http://localhost:8080)

Note that if `service1` is not registered with the Discovery Server yet, then you may get an error because Zuul can't find the route to the base html.  So make sure you start all the services and wait a few min for them all to register themselves and then try again. This is a Eureka configuration issue, not an issue with the Convergent UI Filter.  

Once you have loaded the page and you see how it can pull content from service2, go ahead and kill `service2` and reload the page. You will probably notice that the first two block of the UI still stay populated.  This is because they have been cached.  The 3rd block, the one that uses the dont-cache flag, will show an error immediately.  If you want to see that we aren't lying about the caching, you can log into redis and delete the key for the cache.  It should be something like:

```
redis-cli
127.0.0.1:6379> keys *
1) "service-content::service2:test2"
127.0.0.1:6379> del "service-content::service2:test2"
(integer) 1
```
Then reload the page again and you should see all the sections with errors.  

