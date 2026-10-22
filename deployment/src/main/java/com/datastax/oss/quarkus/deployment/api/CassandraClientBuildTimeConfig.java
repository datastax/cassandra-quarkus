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
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import java.util.List;
import java.util.Optional;

/**
 * This class holds build-time configuration items for the Cassandra Quarkus extension.
 *
 * <p>Settings specified in application.properties under the {@code quarkus.cassandra} prefix will
 * be mapped to fields in this class and its child configuration classes.
 */
@ConfigMapping(prefix = "quarkus.cassandra")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface CassandraClientBuildTimeConfig {

  /**
   * Whether or not an health check is published in case the smallrye-health extension is present.
   */
  @WithName("health.enabled")
  @WithDefault("true")
  boolean healthEnabled();

  /**
   * Whether or not metrics for the Cassandra driver should be published.
   *
   * <p>Note that you need to include additional dependencies in your application when enabling
   * Cassandra metrics.
   *
   * <p>You should add: quarkus-micrometer-registry-prometheus, which will enable metrics globally
   * with reporting via Prometheus, and java-driver-metrics-micrometer, which will enable
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
  @WithName("metrics.enabled")
  @WithDefault("false")
  boolean metricsEnabled();

  /**
   * The name of the algorithm used to compress protocol frames.
   *
   * <p>Valid values are:
   *
   * <ul>
   *   <li><code>none</code>: indicates no compression - this is the default value.
   *   <li><code>lz4</code>: activates compression using LZ4; requires <code>at.yawk.lz4:lz4-java
   *       </code> in the classpath.
   *   <li><code>snappy</code>: activates compression using Snappy; requires <code>
   *       org.xerial.snappy:snappy-java</code> in the classpath. <em>Does not work in Graal Native
   *       mode.</em>
   * </ul>
   */
  @WithName("protocol.compression")
  @WithDefault("none")
  String protocolCompression();

  /** The classes of {@link RequestTracker} implementations to register. */
  Optional<List<String>> requestTrackers();

  /** The classes of {@link NodeStateListener} implementations to register. */
  Optional<List<String>> nodeStateListeners();

  /** The classes of {@link SchemaChangeListener} implementations to register. */
  Optional<List<String>> schemaChangeListeners();

  /**
   * Codecs that have not been found by the discovery and which need registration. This will
   * override any exclusion rules, and it will also work when "discovery" is disabled.
   */
  @WithName("codecs.include")
  Optional<List<String>> includedCodecs();

  /**
   * Codecs that should be excluded from registration during discovery.
   *
   * <p>Each entry is either a fully qualified class name, a package name suffixed with {@code .*}
   * to exclude the codecs of that package, or a package name suffixed with {@code .**} to exclude
   * the codecs of that package and of all its subpackages. For example:
   *
   * <pre>{@code
   * quarkus.cassandra.codecs.exclude=org.acme.MyCodec,org.acme.codecs.*,org.acme.legacy.**
   * }</pre>
   */
  @WithName("codecs.exclude")
  Optional<List<String>> excludedCodecs();

  /** Disables the codec discovery */
  @WithName("codecs.discovery.enabled")
  @WithDefault("true")
  boolean codecDiscoveryEnabled();
}
