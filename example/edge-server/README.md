Convergent# Edge Server

The Edge Server is a Proxy / Load Balancing server that sits in front of all the other applications and will forward requests to the back end services.  

## Running
Running the service for development is as easy as:

```
mvn spring-boot:run
```

Now you can access the UI by going to: `http://localhost:8080`

Note that if `service1` is not registered with Eureka yet, then you may get an error because Zuul can't find the route to the base html.  So make sure you start all the other services and wait a few min for them all to register themselves and then try again. This is a Eureka configuration issue, not an issue with the Convergent UI Filter.  
