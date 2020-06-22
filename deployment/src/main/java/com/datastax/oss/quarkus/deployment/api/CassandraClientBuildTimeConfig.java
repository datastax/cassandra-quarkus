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

  /** Whether or not metrics are published in case the smallrye-metrics extension is present. */
  @ConfigItem(name = "metrics.enabled", defaultValue = "false")
  public boolean metricsEnabled;

  /**
   * List of enabled session-level metrics. They will be taken into account only, if metrics.enabled
   * set to true. If not set, it will default to empty list. For more information, please see
   * java-driver reference.conf.
   */
  @ConfigItem(name = "metrics.session-enabled")
  public Optional<List<String>> metricsSessionEnabled;

  /**
   * List of enabled node-level metrics. They will be taken into account only, if metrics.enabled
   * set to true. If not set, it will default to empty list. For more information, please see
   * java-driver reference.conf.
   */
  @ConfigItem(name = "metrics.node-enabled", defaultValue = "")
  public Optional<List<String>> metricsNodeEnabled;

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

  /**
   * Whether or not the DataStax Java driver should use the Netty event loop provided by the Quarkus
   * framework.
   *
   * <p>When set to true, the driver will not create its own event loops; instead, it will use the
   * main event loop provided by Quarkus. The default is true.
   */
  @ConfigItem(name = "use-quarkus-netty-event-loop", defaultValue = "true")
  public boolean useQuarkusNettyEventLoop;
}
