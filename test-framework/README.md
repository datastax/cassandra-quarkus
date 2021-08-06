# Quarkus Cassandra Test Framework

This module offers one useful class: `CassandraTestResource`, which can be used 
to easily manage Cassandra Docker containers during integration tests.

## Basic usage

`CassandraTestResource` is a Quarkus test resource that starts and stops an 
Apache Cassandra or DataStax DSE container before and after the integration
tests. It is based on the [TestContainers](https://www.testcontainers.org/) 
library.

To enable this feature, simply annotate your integration test classes with
`@QuarkusTestResource(CassandraTestResource.class)`:

```java
@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
public class MyQuarkusIntegrationTest {

  @Test
  public void testFeature1() {
    // interact with the Cassandra container
  }
}
```

Note: don't forget to also annotate the class with `@QuarkusTest`.

If more than one test class is annotated this way, `CassandraTestResource` will
reuse the same container for both test classes, saving your tests time and
computer resources. This can only work however if the container images are
identical. If the container images to use are different (see below), two or more 
different Docker containers will be provisioned.

## Contact points and local datacenter

`CassandraTestResource` automatically sets the contact points and the local 
datacenter to use, by injecting the appropriate values in the following 
properties:

* `quarkus.cassandra.contact-points` 
* `quarkus.cassandra.local-datacenter`

Your tests should not set these properties themselves as they would be 
overridden.

## Initializing your test data

If you want to execute any CQL initialization logic (i.e. execute a  `CREATE 
KEYSPACE` or `CREATE TABLE` query, or insert data in an existing table), create 
an `init_script.cql` file and put it in the test resources folder or your 
project. The CQL script will be automatically executed before the container is
started.

When using different container images as explained above, the CQL script will
be executed against all Docker containers in use. It is not possible to specify
different CQL scripts for different container images.

## Advanced topics

### Customizing the container image

By default, `CassandraTestResource` will pick the `cassandra:latest` Docker 
image. If you want a different image instead, you can do so by defining the 
value of the `quarkus.cassandra.test.container.image` resource arg. 

For example, to use the DataStax DSE image `datastax/dse-server:6.8.14`, do the 
following:

```java
@QuarkusTestResource(
    value = CassandraTestResource.class,
    initArgs =
        @ResourceArg(
            name = "quarkus.cassandra.test.container.image",
            value = "datastax/dse-server:6.8.14"))
public class MyQuarkusIntegrationTest {

  @Test
  public void testFeature1() {
    // interact with the Cassandra container
  }
}
```

### Passing environment variables to the container

It is possible to pass any environment variable to the container through the 
`quarkus.cassandra.test.container.env-vars` arg resource:

```java
@QuarkusTestResource(
    value = CassandraTestResource.class,
    initArgs =
        @ResourceArg(
            name = "quarkus.cassandra.test.container.env-vars",
            value = "MY_VAR1=foo,MY_VAR2=bar"))
public class MyQuarkusIntegrationTest {

  @Test
  public void testFeature1() {
    // interact with the Cassandra container
  }
}
```

Multiple environment variables can be specified. The general syntax is: 
`<var_name1>=<var_value1>,<var_name2>=<var_value2>,...,<var_nameN>=<var_valueN>`.

The default environment variables passed to the container aim to make Cassandra 
start up as fast as possible while using as few resources as possible; these 
are:

```
CASSANDRA_SNITCH = PropertyFileSnitch
HEAP_NEWSIZE = 128M
MAX_HEAP_SIZE = 1024M
DS_LICENSE = accept
```

### Passing options to the Cassandra JVM

It is possible to pass JVM options to the Cassandra process through the
`quarkus.cassandra.test.container.jvm-opts` arg resource:

```java
@QuarkusTestResource(
    value = CassandraTestResource.class,
    initArgs =
        @ResourceArg(
            name = "quarkus.cassandra.test.container.jvm-opts",
            value = "-Doption1=value1 -XX:+HeapDumpOnOutOfMemoryError"))
public class MyQuarkusIntegrationTest {

  @Test
  public void testFeature1() {
    // interact with the Cassandra container
  }
}
```

Any valid JVM option can be specified. The default JVM options aim to make 
Cassandra start up as fast as possible; these are:

```
-Dcassandra.skip_wait_for_gossip_to_settle=0 
-Dcassandra.num_tokens=1 
-Dcassandra.initial_token=0
```

### Customizing the container startup command

It is possible to set the command that should be run in the container through 
the `quarkus.cassandra.test.container.cmd` arg resource. 

By default, the container executes whatever command is specified in the 
container image's entrypoint. 

Note: DSE images usually do not allow you to override the start command; you can
only append arguments to the built-in `dse cassandra -f` command. For example, 
to make a DSE 6.8 image start up with Graph enabled, simply specify `-g` as the 
command to run; this switch triggers the initialization of DSE Graph and will be 
appended to the `dse cassandra -f` command:

```java
@QuarkusTestResource(
    value = CassandraTestResource.class,
    initArgs = {
      @ResourceArg(
          name = "quarkus.cassandra.test.container.image",
          value = "datastax/dse-server:6.8.14"),
      @ResourceArg(
          name = "quarkus.cassandra.test.container.cmd", 
          value = "-g") // command will be: dse cassandra -f -g
    })
public class MyQuarkusIntegrationTest {

  @Test
  public void testGraph() {
    // interact with DSE Graph
  }
}
```

Commands should be provided in one single string format (arguments will be 
automatically split on spaces).
