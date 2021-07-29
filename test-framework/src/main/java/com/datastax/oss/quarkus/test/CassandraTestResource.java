/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.quarkus.test;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.wait.CassandraQueryWaitStrategy;
import org.testcontainers.utility.DockerImageName;

/**
 * A {@link QuarkusTestResourceLifecycleManager} that starts and stops a {@link CassandraContainer}.
 *
 * <p>Integration tests using this resource must define two settings in the .properties file:
 *
 * <pre>
 * quarkus.cassandra.contact-points=${quarkus.cassandra.docker_host}:${quarkus.cassandra.docker_port}
 * quarkus.cassandra.local-datacenter=datacenter1
 * </pre>
 *
 * Please note that contact points must not be hard-coded, but instead, specified exactly as <code>
 * ${quarkus.cassandra.docker_host}:${quarkus.cassandra.docker_port}</code> - the actual host and
 * port will be automatically injected by this manager.
 *
 * <p>If you want to execute a CQL init logic (i.e. CREATE KEYSPACE or CREATE TABLE query) please
 * create an <code>init_script.cql</code> file and put it in the test resources folder.
 */
public class CassandraTestResource implements QuarkusTestResourceLifecycleManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(CassandraTestResource.class);

  private static CassandraContainer<?> cassandraContainer;

  @Override
  public Map<String, String> start() {
    cassandraContainer =
        new CassandraContainer<>(DockerImageName.parse("cassandra").withTag("4.0.0"))
            .withEnv("CASSANDRA_SNITCH", "PropertyFileSnitch")
            .withEnv(
                "JVM_OPTS",
                "-Dcassandra.skip_wait_for_gossip_to_settle=0 "
                    + "-Dcassandra.num_tokens=1 "
                    + "-Dcassandra.initial_token=0")
            .withEnv("HEAP_NEWSIZE", "128M")
            .withEnv("MAX_HEAP_SIZE", "1024M");

    // set init script only if it's provided by the caller
    URL resource = Thread.currentThread().getContextClassLoader().getResource("init_script.cql");
    if (resource != null) {
      cassandraContainer.withInitScript("init_script.cql");
    }
    cassandraContainer.setWaitStrategy(new CassandraQueryWaitStrategy());
    cassandraContainer.start();
    String exposedPort =
        String.valueOf(cassandraContainer.getMappedPort(CassandraContainer.CQL_PORT));
    String exposedHost = cassandraContainer.getContainerIpAddress();
    if (exposedHost.equals("localhost")) {
      exposedHost = "127.0.0.1";
    }
    LOGGER.info(
        "Started {} on {}:{}", cassandraContainer.getDockerImageName(), exposedHost, exposedPort);
    HashMap<String, String> result = new HashMap<>();
    result.put("quarkus.cassandra.docker_host", exposedHost);
    result.put("quarkus.cassandra.docker_port", exposedPort);
    return result;
  }

  @Override
  public void stop() {
    if (cassandraContainer != null && cassandraContainer.isRunning()) {
      cassandraContainer.stop();
    }
  }
}
