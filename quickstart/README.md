# Quarkus demo: Cassandra Client

This example showcases how to use the Cassandra client with Quarkus. 

# Cassandra instance - Running with Docker

Just run it as follows (only 9042 port is required for quickstart application):
```shell script
docker run \
   --name local-cassandra-instance \
   -p 7000:7000 \
   -p 7001:7001 \
   -p 7199:7199 \
   -p 9042:9042 \
   -p 9160:9160 \
   -p 9404:9404 \
   -d \
   launcher.gcr.io/google/cassandra3
```

**Provision table**

```shell script
docker exec -it local-cassandra-instance cqlsh -e "CREATE KEYSPACE IF NOT EXISTS k1 WITH replication = {'class':'SimpleStrategy', 'replication_factor':1}"
```
```shell script
docker exec -it local-cassandra-instance cqlsh -e "CREATE TABLE IF NOT EXISTS k1.fruit(id text, name text, description text, PRIMARY KEY((id), name))"
```
                                                     

# Run the demo on dev mode

- Run `mvn clean package` and then `java -jar ./target/quickstart-1.0.0-SNAPSHOT-runner.jar`
- In dev mode `mvn clean quarkus:dev`

Go to `http://localhost:8080/fruits.html`, it should show a simple App to manage list of Fruits. 
You can add fruits to the list via the form.

Alternatively, you can use curl commands to interact with the underlying REST API.
To create a fruit:
```shell script
curl --header "Content-Type: application/json" \
  --request POST \
  --data '{"name":"curl_product","description":"this was created via curl"}' \
  http://localhost:8080/fruits
```
To retrieve fruits:
```shell script
curl -X GET http://localhost:8080/fruits
```

# Running in native

You can compile the application into a native binary using:

`mvn clean package -Pnative`

and run with:

`./target/quickstart-1.0.0-SNAPSHOT-runner` 