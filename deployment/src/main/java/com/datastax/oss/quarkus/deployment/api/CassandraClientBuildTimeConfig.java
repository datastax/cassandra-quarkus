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
package com.datastax.oss.quarkus.deployment.api;

import com.datastax.oss.driver.api.core.metadata.NodeStateListener;
import com.datastax.oss.driver.api.core.metadata.schema.SchemaChangeListener;
import com.datastax.oss.driver.api.core.tracker.RequestTracker;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import java.util.List;
import java.util.Optional;

/**
 * This class holds build-time configuration items for the Cassandra Quarkus extension.
 *
 * <p>Settings specified in application.properties under the {@code quarkus.cassandra} prefix will
 * be mapped to fields in this class and its child configuration classes.
 */
@ConfigRoot(name = "cassandra", phase = ConfigPhase.BUILD_TIME)
public class CassandraClientBuildTimeConfig {

  /**
   * Whether or not an health check is published in case the smallrye-health extension is present.
   */
  @ConfigItem(name = "health.enabled", defaultValue = "true")
  public boolean healthEnabled;

  /**
   * Whether or not metrics for the Cassandra driver should be published.
   *
   * <p>Note that you need to include additional dependencies in your application when enabling
   * Cassandra metrics.
   *
   * <p>If you are using Micrometer, you should add: quarkus-micrometer-registry-prometheus, which
   * will enable metrics globally with reporting via Prometheus, and java-driver-metrics-micrometer,
   * which will enable driver-specific metrics to be reported.
   *
   * <p>If you are using MicroProfile metrics, you should add: quarkus-smallrye-metrics, which will
   * enable metrics globally, and java-driver-metrics-microprofile, which will enable
   * driver-specific metrics to be reported.
   *
   * <p>Lastly, you can also customize which session-level and node-level metrics you wish the
   * driver to track for you. This is done with two other properties: session.enabled and
   * node.enabled respectively. For example:
   *
   * <pre>{@code
   * quarkus.cassandra.metrics.enabled=true
   * quarkus.cassandra.metrics.session.enabled=cql-requests,cql-client-timeouts
   * quarkus.cassandra.metrics.node.enabled=pool.open-connections,pool.in-flight,cql-messages
   * }</pre>
   *
   * If you don't specify the session-level or the node-level metrics to track, a default list of
   * useful metrics will be used.
   *
   * <p>For more information on available metrics, see <a
   * href="https://docs.datastax.com/en/developer/java-driver/latest/manual/core/metrics/#configuration">
   * Metrics configuration</a> in the Java driver manual.
   */
  @ConfigItem(name = "metrics.enabled", defaultValue = "false")
  public boolean metricsEnabled;

  /**
   * The name of the algorithm used to compress protocol frames.
   *
   * <p>Valid values are:
   *
   * <ul>
   *   <li><code>none</code>: indicates no compression - this is the default value.
   *   <li><code>lz4</code>: activates compression using LZ4; requires <code>org.lz4:lz4-java</code>
   *       in the classpath.
   *   <li><code>snappy</code>: activates compression using Snappy; requires <code>
   *       org.xerial.snappy:snappy-java</code> in the classpath. <em>Does not work in Graal Native
   *       mode.</em>
   * </ul>
   */
  @ConfigItem(name = "protocol.compression", defaultValue = "none")
  public String protocolCompression;

  /** The classes of {@link RequestTracker} implementations to register. */
  @ConfigItem(name = "request-trackers")
  public Optional<List<String>> requestTrackers;

  /** The classes of {@link NodeStateListener} implementations to register. */
  @ConfigItem(name = "node-state-listeners")
  public Optional<List<String>> nodeStateListeners;

  /** The classes of {@link SchemaChangeListener} implementations to register. */
  @ConfigItem(name = "schema-change-listeners")
  public Optional<List<String>> schemaChangeListeners;
}
