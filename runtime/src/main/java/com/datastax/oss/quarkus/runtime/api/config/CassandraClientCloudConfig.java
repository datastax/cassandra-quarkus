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
import java.util.Optional;

/** This class holds runtime configuration items related to DataStax Astra cloud clusters. */
@ConfigGroup
public class CassandraClientCloudConfig {

  /**
   * The path to a cloud secure bundle used to connect to DataStax Astra.
   *
   * <p>This setting is not required to connect to connect to Apache Cassandra (R) or DataStax
   * Enterprise (DSE).
   */
  @ConfigItem(name = "secure-connect-bundle")
  public Optional<Path> secureConnectBundle;
}
