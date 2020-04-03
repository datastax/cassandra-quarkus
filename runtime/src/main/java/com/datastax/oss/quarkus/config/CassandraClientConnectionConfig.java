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
package com.datastax.oss.quarkus.config;

import com.datastax.oss.driver.api.core.CqlSession;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@ConfigGroup
public class CassandraClientConnectionConfig {

  /**
   * Contact-points used to connect to Apache Cassandra (R). If not specified, it will connect to
   * localhost.
   */
  @ConfigItem(name = "contact-points", defaultValue = "127.0.0.1:9042")
  public List<String> contactPoints;

  /** Local datacenter used when creating a {@link CqlSession}. */
  @ConfigItem(name = "load-balancing-policy.local-datacenter")
  public String localDatacenter;

  /** How long the driver waits for a request to complete. */
  @ConfigItem(name = "request.timeout")
  public Optional<Duration> requestTimeout;
}
