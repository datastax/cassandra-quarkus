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

/** This class holds runtime configuration items related to connection settings. */
@ConfigGroup
public class CassandraClientConnectionConfig {

  /**
   * Contact-points used to connect to Apache Cassandra (R) or DataStax Enterprise (DSE).
   *
   * <p>If not specified, the driver will attempt to connect to localhost on port 9042.
   *
   * <p>This setting is not required to connect to DataStax Astra.
   */
  @ConfigItem(name = "contact-points")
  public Optional<List<String>> contactPoints;

  /**
   * Local datacenter used to connect to Apache Cassandra (R) or DataStax Enterprise (DSE).
   *
   * <p>This setting is not required to connect to DataStax Astra.
   */
  @ConfigItem(name = "local-datacenter")
  public Optional<String> localDatacenter;

  /**
   * The name of the keyspace that the session should initially be connected to.
   *
   * <p>This expects the same format as in a CQL query: case-sensitive names must be quoted. For
   * example:
   *
   * <pre>
   * quarkus.cassandra.keyspace = case_insensitive_name
   * quarkus.cassandra.keyspace = \"CaseSensitiveName\"
   * </pre>
   *
   * <p>If this option is absent, the session won't be connected to any keyspace, and you'll have to
   * either qualify table names in your queries, or use the per-query keyspace feature available in
   * Cassandra 4 and above.
   */
  public Optional<String> keyspace;
}
