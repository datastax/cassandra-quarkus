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
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@ConfigGroup
public class CassandraClientConnectionConfig {

  /**
   * Contact-points used to connect to Apache Cassandra (R) or DataStax Enterprise (DSE). If not
   * specified, the driver will attempt to connect to localhost on port 9042. This setting is not
   * required to connect to DataStax Astra.
   */
  @ConfigItem(name = "contact-points")
  public Optional<List<String>> contactPoints;

  /**
   * Local datacenter used to connect to Apache Cassandra (R) or DataStax Enterprise (DSE). This
   * setting is not required to connect to DataStax Astra.
   */
  @ConfigItem(name = "local-datacenter")
  public Optional<String> localDatacenter;

  /**
   * The path to a cloud secure bundle used to connect to DataStax Astra. This setting is not
   * required to connect to connect to Apache Cassandra (R) or DataStax Enterprise (DSE).
   */
  @ConfigItem(name = "secure-connect-bundle")
  public Optional<Path> secureConnectBundle;

  /** How long the driver waits for a request to complete. */
  @ConfigItem(name = "request.timeout")
  public Optional<Duration> requestTimeout;

  /**
   * The username used to connect to Apache Cassandra(R). If a username and a password are both
   * provided, plain text authentication will be automatically enabled.
   */
  @ConfigItem(name = "username")
  public Optional<String> username;

  /**
   * The auth_provider password used to connect to Apache Cassandra(R). If a username and a password
   * are both provided, plain text authentication will be automatically enabled.
   */
  @ConfigItem(name = "password")
  public Optional<String> password;
}
