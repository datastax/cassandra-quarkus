# Cassandra Quarkus

An Apache Cassandra(R) extension for Quarkus.

## Running integration tests

To run the non-native integration tests simply execute:

    mvn clean verify
    
To run the native integration tests as well, execute:

    mvn clean verify -Dnative
    
You need to point the environment variable `GRAALVM_HOME` to a valid Graal 
installation root.
