# TESLA Camera Server
TESLA-based server for the demo camera application. With this server, you can query the list of registered devices from a REST API, then broadcast messages, asking the mobiles to capture images and send them to the web application to store them.

To compile and run the project, read the documentation below:
## Requirements
* Apache Maven 3
* Java 8

## Compile
`mvn clean package`

## Run the application
To run the server, you must provide the following things as command line arguments:
* the broadcast address
* the broadcast port
* the address of the web application (to query data from)

Then you can run the server like this:
`java -jar target/tesla-camera-server-1.0-SNAPSHOT-jar-with-dependencies.jar 230.1.2.3 9999 http://localhost:8080/`