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
import java.util.Collections;
import java.util.Map;
import org.jboss.logging.Logger;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.wait.CassandraQueryWaitStrategy;

/**
 * The integration test that is using this resources should have defined two settings in the
 * .properties file:
 *
 * <pre>
 * quarkus.cassandra.contact-points=127.0.0.1:${quarkus.cassandra.docker_port}
 * quarkus.cassandra.load-balancing-policy.local-datacenter=datacenter1
 * </pre>
 *
 * Please note the ${quarkus.cassandra.docker_port} - it will be automatically injected by the
 * {@code CassandraTestResource}.
 *
 * <p>If you want to execute the CQL init logic (i.e. CREATE KEYSPACE or CREATE TABLE) please create
 * an init_script.cql file and put it in the resources folder.
 */
public class CassandraTestResource implements QuarkusTestResourceLifecycleManager {
  private static final Logger LOGGER = Logger.getLogger(CassandraTestResource.class);
  private static CassandraContainer<?> cassandraContainer;

  @Override
  public Map<String, String> start() {
    cassandraContainer = new CassandraContainer<>();
    cassandraContainer.withInitScript("init_script.cql");
    cassandraContainer.setWaitStrategy(new CassandraQueryWaitStrategy());
    cassandraContainer.start();
    String exposedPort =
        String.valueOf(cassandraContainer.getMappedPort(CassandraContainer.CQL_PORT));
    LOGGER.infof("Started %s on port %s", cassandraContainer.getDockerImageName(), exposedPort);
    return Collections.singletonMap("quarkus.cassandra.docker_port", exposedPort);
  }

  @Override
  public void stop() {
    if (cassandraContainer != null && cassandraContainer.isRunning()) {
      cassandraContainer.stop();
    }
  }
}
