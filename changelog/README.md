### 1.1.2 (in progress)

This release is built against Quarkus 2.1.0.Final and the Cassandra driver 4.13.0.

- [new feature] Ability to specify container image and startup options in integration tests (fixes #192)

### 1.1.1

This release is built against Quarkus 2.1.0.Final and the Cassandra driver 4.13.0.

- [new feature] Ability to configure listeners from Quarkus configuration (fixes #182)
- [improvement] Upgrade to Quarkus 2.1.0.Final

Merged from 1.0.x:

- [bug] Reactive "if not exists" insertion does not work (fixes #183)
- [improvement] Include application.conf in the native image (fixes #188)

### 1.1.0

This release is built against Quarkus 2.0.0.Final and the Cassandra driver 4.12.0.

- [improvement] Upgrade to Quarkus 2.0.0.Final

### 1.1.0-rc2

This release is built against Quarkus 2.0.0.RC3 and the Cassandra driver 4.12.0.

Merged from 1.0.x:

- [bug] Do not reference MutinyResultProducerService directly (fixes #178)

### 1.1.0-rc1

This release is built against Quarkus 2.0.0.RC3 and the Cassandra driver 4.12.0.

- [improvement] Upgrade to Quarkus 2.0.0.RC3 (fixes #172)
  
Merged from 1.0.x:

- [bug] Register MutinyResultProducerService as ServiceProviderBuildItem (fixes #174)
- [bug] Use Thread context class loader in QuarkusDriverContext (fixes #171)

### 1.0.4 (in progress)

This release is built against Quarkus 1.13.7.Final and the Cassandra driver 4.11.3.

- [bug] Reactive "if not exists" insertion does not work (fixes #183)
- [improvement] Include application.conf in the native image (fixes #188)

### 1.0.3

This release is built against Quarkus 1.13.7.Final and the Cassandra driver 4.11.2.

- [bug] Do not reference MutinyResultProducerService directly (fixes #178)

### 1.0.2

This release is built against Quarkus 1.13.7.Final and the Cassandra driver 4.11.2.

- [bug] Register MutinyResultProducerService as ServiceProviderBuildItem (fixes #174)
- [bug] Use Thread context class loader in QuarkusDriverContext (fixes #171)

### 1.0.1

- [improvement] Enable most useful metrics by default (fixes #164)
- [improvement] Upgrade driver to 4.11.0
- [improvement] Upgrade Quarkus to 1.12.2.Final
- [improvement] Merge runtime and deployment boms (fixes #162)
- [new feature] Adapt Quarkus Cassandra metrics to JAVA-2872 (fixes #160)
- [bug] Register DefaultMetricsFactory for reflection when metrics disabled (fixes #157)
- [bug] Make Tinkerpop truly optional (fixes #146)

### 1.0.0

- [new feature] Add support for Micrometer (fixes #150)
- [new feature] Eager mapper and DAO initialization at startup (fixes #112)
- [new feature] Automatic mapper and DAO detection and registration (fixes #113)
- [improvement] Simplify quickstart application data model (fixes #121)
- [documentation] Move the quickstart guide to the quickstart module (fixes #114)
- [bug] Prevent eager init from making the application unresponsive (fixes #144)
- [improvement] Use driver's own MicroProfileMetricsFactory
- [improvement] Replace references to Capabilities.METRICS by MetricsCapabilityBuildItem
- [improvement] Upgrade driver to 4.10.0
- [bug] Add failure recovery to Mutiny result set before subscribing
- [new feature] Provide a lazy wrapper for QuarkusCqlSession (fixes #106)
- [improvement] Upgrade metrics integration to incorporate changes from JAVA-2808 (fixes #117)
- [new feature] Enable SSL support (fixes #111)
- [bug] Don't call close() if session wasn't initialized (fixes #125)
- [bug] Avoid implementing Multi and extend AbstractMulti instead (#115)

### 1.0.0-alpha3

- [improvement] Make SmallRye Metrics and Health dependencies non-optional

### 1.0.0-alpha2

- [improvement] Add more configuration settings (fixes #72, fixes #105)
- [improvement] Exclude Groovy jars when importing the driver (fixes #101)
- [bug] Trigger eager session initialization at startup (fixes #99)

### 1.0.0-alpha1

- [new feature] Add setting for DataStax Astra secure connect bundle (fixes #94)
- [new feature] Add configuration options for plain text authentication
- [improvement] JAVA-2778: Split classes into api and internal packages
- [improvement] JAVA-2722: Replace generated code in CassandraClientProcessor by normal java class
- [new feature] JAVA-2754: Integration between Driver reactive API and Quarkus
- [new feature] JAVA-2719: Integrate with Netty event loop provided by Quarkus
- [documentation] JAVA-2684: Add documentation and examples of Quarkus Cassandra extension
- [new feature] JAVA-2683: Create a Quarkus Cassandra quick starter
- [improvement] JAVA-2707: Substitute optional dependencies with Quarkus Substitution
- [new feature] JAVA-2694: Add Metrics to the extension
- [new feature] JAVA-2696: Native Graal support with Quarkus
- [new feature] JAVA-2693: Add HealthChecks to extension
- [new feature] JAVA-2682: Create a Quarkus Cassandra extension
