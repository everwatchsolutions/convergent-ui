# Discovery Server

As part of the Spring Cloud Infrastructure, the Discover Service is a service registry that other services can advertise themselves with.  When services come up, they can register with the Discovery Service and other services can lookup services in the registry as well. 

## Running

Running it in development, just do:

mvn clean spring-boot:run

It will then be running on [http://localhost:8761](http://localhost:8761)

You can view registered services at that webpage.  You can view detailed info about the application at: [http://localhost:8761/eureka/apps](http://localhost:8761/eureka/apps)
