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

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import io.smallrye.config.WithParentName;

/**
 * This class holds runtime configuration items for the Cassandra Quarkus extension.
 *
 * <p>Settings specified in application.properties under the {@code quarkus.cassandra} prefix will
 * be mapped to fields in this class and its child configuration classes.
 */
@ConfigMapping(prefix = "quarkus.cassandra")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface CassandraClientConfig {
  /** The client connection configuration settings. */
  @WithParentName
  CassandraClientConnectionConfig cassandraClientConnectionConfig();

  /** The metrics settings. */
  @WithName("metrics")
  CassandraClientMetricsConfig cassandraClientMetricsConfig();

  /** The cloud (DataStax Astra) configuration settings. */
  @WithName("cloud")
  CassandraClientCloudConfig cassandraClientCloudConfig();

  /** The session initialization settings. */
  @WithName("init")
  CassandraClientInitConfig cassandraClientInitConfig();

  /** The authentication settings. */
  @WithName("auth")
  CassandraClientAuthConfig cassandraClientAuthConfig();

  /** The request settings. */
  @WithName("request")
  CassandraClientRequestConfig cassandraClientRequestConfig();

  /** The DSE Graph settings. */
  @WithName("graph")
  CassandraClientGraphConfig cassandraClientGraphConfig();
}
