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
package com.datastax.oss.quarkus.runtime.api.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import java.util.List;
import java.util.Optional;

/** This class holds runtime configuration items related to metrics. */
@ConfigGroup
public class CassandraClientMetricsConfig {

  /**
   * The metric names specified here will be taken into account only if metrics are globally
   * enabled, that is, if {@code quarkus.cassandra.metrics.enabled = true}.
   *
   * <p>If this setting is not present, it will default to empty list and no node-level metrics will
   * be available.
   *
   * <p>Example configuration:
   *
   * <pre>{@code
   * quarkus.cassandra.metrics.enabled=true
   * quarkus.cassandra.metrics.session.enabled=cql-requests,cql-client-timeouts
   * quarkus.cassandra.metrics.node.enabled=pool.open-connections,pool.in-flight,cql-messages
   * }</pre>
   *
   * <p>For more information on available metrics, see <a
   * href="https://docs.datastax.com/en/developer/java-driver/latest/manual/core/metrics/#configuration">
   * Metrics configuration</a> in the Java driver manual.
   */
  @ConfigItem(name = "session.enabled")
  public Optional<List<String>> enabledSessionMetrics;

  /**
   * List of node-level metric names to enable.
   *
   * <p>The metric names specified here will be taken into account only if metrics are globally
   * enabled, that is, if {@code quarkus.cassandra.metrics.enabled = true}.
   *
   * <p>If this setting is not present, it will default to empty list and no node-level metrics will
   * be available.
   *
   * <p>Example configuration:
   *
   * <pre>{@code
   * quarkus.cassandra.metrics.enabled=true
   * quarkus.cassandra.metrics.session.enabled=cql-requests,cql-client-timeouts
   * quarkus.cassandra.metrics.node.enabled=pool.open-connections,pool.in-flight,cql-messages
   * }</pre>
   *
   * <p>For more information on available metrics, see <a
   * href="https://docs.datastax.com/en/developer/java-driver/latest/manual/core/metrics/#configuration">
   * Metrics configuration</a> in the Java driver manual.
   */
  @ConfigItem(name = "node.enabled")
  public Optional<List<String>> enabledNodeMetrics;
}
