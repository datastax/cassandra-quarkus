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
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.wait.CassandraQueryWaitStrategy;

/**
 * A {@link QuarkusTestResourceLifecycleManager} that starts and stops a {@link CassandraContainer}.
 *
 * <p>Integration tests using this resource must define two settings in the .properties file:
 *
 * <pre>
 * quarkus.cassandra.contact-points=127.0.0.1:${quarkus.cassandra.docker_port}
 * quarkus.cassandra.local-datacenter=datacenter1
 * </pre>
 *
 * Please note that ports for contact points must not be hard-coded, but instead, specified exactly
 * as <code>${quarkus.cassandra.docker_port}</code> - the actual port will be automatically injected
 * by this manager.
 *
 * <p>If you want to execute a CQL init logic (i.e. CREATE KEYSPACE or CREATE TABLE query) please
 * create an <code>init_script.cql</code> file and put it in the test resources folder.
 */
public class CassandraTestResource implements QuarkusTestResourceLifecycleManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(CassandraTestResource.class);

  private static CassandraContainer<?> cassandraContainer;

  @Override
  public Map<String, String> start() {
    cassandraContainer = new CassandraContainer<>();
    // set init script only if it's provided by the caller
    URL resource = Thread.currentThread().getContextClassLoader().getResource("init_script.cql");
    if (resource != null) {
      cassandraContainer.withInitScript("init_script.cql");
    }
    cassandraContainer.setWaitStrategy(new CassandraQueryWaitStrategy());
    cassandraContainer.start();
    String exposedPort =
        String.valueOf(cassandraContainer.getMappedPort(CassandraContainer.CQL_PORT));
    LOGGER.info("Started {} on port {}", cassandraContainer.getDockerImageName(), exposedPort);
    return Collections.singletonMap("quarkus.cassandra.docker_port", exposedPort);
  }

  @Override
  public void stop() {
    if (cassandraContainer != null && cassandraContainer.isRunning()) {
      cassandraContainer.stop();
    }
  }
}
