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
   * <ul>
   *   <li>lz4: requires org.lz4:lz4-java in the classpath.
   *   <li>snappy: requires org.xerial.snappy:snappy-java in the classpath. Works only when not in
   *       Native mode.
   *   <li>the string "none" to indicate no compression - this is a default value.
   * </ul>
   */
  @ConfigItem(name = "protocol.compression", defaultValue = "none")
  public String protocolCompression;

  /**
   * Whether or not the DataStax Java driver should use the Netty event loop provided by the Quarkus
   * framework. The default is true, meaning that the Java driver will not create event loops
   * itself. When set to true, {@code QuarkusCqlSession} will use the main event loop provided by
   * Quarkus.
   */
  @ConfigItem(name = "use-quarkus-netty-event-loop", defaultValue = "true")
  public boolean useQuarkusNettyEventLoop;
}
