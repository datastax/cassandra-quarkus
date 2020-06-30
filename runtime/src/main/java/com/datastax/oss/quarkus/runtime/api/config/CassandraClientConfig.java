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

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

/**
 * This class holds runtime configuration items for the Cassandra Quarkus extension.
 *
 * <p>Settings specified in application.properties under the {@code quarkus.cassandra} prefix will
 * be mapped to fields in this class and its child configuration classes.
 */
@ConfigRoot(name = CassandraClientConfig.CONFIG_NAME, phase = ConfigPhase.RUN_TIME)
public class CassandraClientConfig {

  public static final String CONFIG_NAME = "cassandra";

  /** The client connection configuration settings. */
  @ConfigItem(name = ConfigItem.PARENT)
  public CassandraClientConnectionConfig cassandraClientConnectionConfig;

  /** The cloud (DataStax Astra) configuration settings. */
  @ConfigItem(name = "cloud")
  public CassandraClientCloudConfig cassandraClientCloudConfig;

  /** The session initialization settings. */
  @ConfigItem(name = "init")
  public CassandraClientInitConfig cassandraClientInitConfig;

  /** The authentication settings. */
  @ConfigItem(name = "auth")
  public CassandraClientAuthConfig cassandraClientAuthConfig;

  /** The request settings. */
  @ConfigItem(name = "request")
  public CassandraClientRequestConfig cassandraClientRequestConfig;

  /** The DSE Graph settings. */
  @ConfigItem(name = "graph")
  public CassandraClientGraphConfig cassandraClientGraphConfig;
}
