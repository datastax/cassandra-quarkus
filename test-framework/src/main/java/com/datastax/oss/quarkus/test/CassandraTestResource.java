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

import com.datastax.driver.core.Host;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.wait.CassandraQueryWaitStrategy;
import org.testcontainers.shaded.com.google.common.base.Splitter;
import org.testcontainers.shaded.com.google.common.base.Splitter.MapSplitter;
import org.testcontainers.utility.DockerImageName;

/**
 * A {@link QuarkusTestResourceLifecycleManager} that starts and stops a {@link CassandraContainer}.
 */
public class CassandraTestResource implements QuarkusTestResourceLifecycleManager {

  public static final String QUARKUS_CASSANDRA_CONTAINER_IMAGE_KEY =
      "quarkus.cassandra.test.container.image";

  public static final String QUARKUS_CASSANDRA_CONTAINER_ENV_KEY =
      "quarkus.cassandra.test.container.env-vars";

  public static final String QUARKUS_CASSANDRA_CONTAINER_CMD_KEY =
      "quarkus.cassandra.test.container.cmd";

  public static final String QUARKUS_CASSANDRA_CONTAINER_JVM_OPTS_KEY =
      "quarkus.cassandra.test.container.jvm-opts";

  private static final String QUARKUS_CASSANDRA_CONTAINER_IMAGE_DEFAULT = "cassandra:latest";

  private static final String QUARKUS_CASSANDRA_CONTAINER_ENV_DEFAULT =
      "CASSANDRA_SNITCH = PropertyFileSnitch, "
          + "HEAP_NEWSIZE = 128M, "
          + "MAX_HEAP_SIZE = 1024M, "
          + "DS_LICENSE = accept";

  private static final String QUARKUS_CASSANDRA_CONTAINER_JVM_OPTS_DEFAULT =
      "-Dcassandra.skip_wait_for_gossip_to_settle=0 "
          + "-Dcassandra.num_tokens=1 "
          + "-Dcassandra.initial_token=0";

  private static final String QUARKUS_CASSANDRA_CONTACT_POINTS = "quarkus.cassandra.contact-points";
  private static final String QUARKUS_CASSANDRA_LOCAL_DATACENTER =
      "quarkus.cassandra.local-datacenter";

  private static final MapSplitter ENV_ENTRIES_SPLITTER =
      Splitter.on(",").trimResults().withKeyValueSeparator(Splitter.on("=").trimResults());

  private static final Logger LOGGER = LoggerFactory.getLogger(CassandraTestResource.class);

  private volatile CassandraContainer<?> cassandraContainer;

  @Override
  public void init(Map<String, String> initArgs) {
    String image =
        initArgs.getOrDefault(
            QUARKUS_CASSANDRA_CONTAINER_IMAGE_KEY, QUARKUS_CASSANDRA_CONTAINER_IMAGE_DEFAULT);
    DockerImageName dockerImage =
        DockerImageName.parse(image).asCompatibleSubstituteFor("cassandra");
    String envString =
        initArgs.getOrDefault(
            QUARKUS_CASSANDRA_CONTAINER_ENV_KEY, QUARKUS_CASSANDRA_CONTAINER_ENV_DEFAULT);
    Map<String, String> env = new HashMap<>(ENV_ENTRIES_SPLITTER.split(envString));
    String jvmOptionsString =
        initArgs.getOrDefault(
            QUARKUS_CASSANDRA_CONTAINER_JVM_OPTS_KEY, QUARKUS_CASSANDRA_CONTAINER_JVM_OPTS_DEFAULT);
    env.put("JVM_OPTS", jvmOptionsString);
    cassandraContainer = new CassandraContainer<>(dockerImage).withEnv(env);
    // set init script only if it's provided by the caller
    URL resource = Thread.currentThread().getContextClassLoader().getResource("init_script.cql");
    if (resource != null) {
      cassandraContainer.withInitScript("init_script.cql");
    }
    cassandraContainer.setWaitStrategy(new CassandraQueryWaitStrategy());
    String cmd = initArgs.get(QUARKUS_CASSANDRA_CONTAINER_CMD_KEY);
    if (cmd != null) {
      cassandraContainer.setCommand(cmd);
    }
  }

  @Override
  public Map<String, String> start() {
    LOGGER.info("Container {} starting...", cassandraContainer.getDockerImageName());
    cassandraContainer.start();
    String contactPoint = getContactPoint();
    String localDc = getLocalDatacenter();
    if (localDc != null) {
      LOGGER.info(
          "Container {} listening on {} (inferred local DC: {})",
          cassandraContainer.getDockerImageName(),
          contactPoint,
          localDc);
      return Map.of(
          QUARKUS_CASSANDRA_CONTACT_POINTS,
          contactPoint,
          QUARKUS_CASSANDRA_LOCAL_DATACENTER,
          localDc);
    } else {
      LOGGER.info(
          "Container {} listening on {}", cassandraContainer.getDockerImageName(), contactPoint);
      return Map.of(QUARKUS_CASSANDRA_CONTACT_POINTS, contactPoint);
    }
  }

  @Override
  public void stop() {
    if (cassandraContainer != null && cassandraContainer.isRunning()) {
      LOGGER.info("Container {} stopping...", cassandraContainer.getDockerImageName());
      cassandraContainer.stop();
      LOGGER.info("Container {} stopped", cassandraContainer.getDockerImageName());
    }
  }

  private String getContactPoint() {
    String host = cassandraContainer.getContainerIpAddress();
    if (host.equals("localhost")) {
      host = "127.0.0.1";
    }
    int port = cassandraContainer.getMappedPort(CassandraContainer.CQL_PORT);
    return host + ":" + port;
  }

  private String getLocalDatacenter() {
    for (Host host : cassandraContainer.getCluster().getMetadata().getAllHosts()) {
      String dc = host.getDatacenter();
      if (dc != null) {
        return dc;
      }
    }
    LOGGER.warn(
        "Could not determine local datacenter for container {}",
        cassandraContainer.getDockerImageName());
    return null;
  }
}
