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

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import java.time.Duration;
import java.util.Optional;

/** This class holds runtime configuration items related to DSE Graph settings. */
@ConfigGroup
public class CassandraClientGraphConfig {

  /** The name of the graph targeted by graph statements. */
  @ConfigItem(name = "name")
  public Optional<String> graphName;

  /**
   * How long the driver waits for a graph request to complete.
   *
   * <p>This is a global limit on the duration of a query, including any internal retries the driver
   * might do. Graph statements behave a bit differently than regular CQL requests: by default, the
   * client timeout is not set, and the driver will just wait as long as needed until the server
   * replies (which is itself governed by server-side timeout configuration).
   *
   * <p>If you specify a client timeout with this option, then the driver will fail the request
   * after the given time; note that the value is also sent along with the request, so that the
   * server can also time out early and avoid wasting resources on a response that the client has
   * already given up on.
   *
   * <p>If this value is left unset (default) or is explicitly set to zero, no timeout will be
   * applied.
   */
  @ConfigItem(name = "request.timeout")
  public Optional<Duration> graphRequestTimeout;

  /**
   * The read consistency level to use for graph statements. If not specified, it defaults to {@link
   * ConsistencyLevel#LOCAL_QUORUM}.
   *
   * <p>DSE Graph is able to distinguish between read and write timeouts for the internal storage
   * queries that will be produced by a traversal. Hence the consistency level for reads and writes
   * can be set separately.
   */
  @ConfigItem(name = "read-consistency-level")
  public Optional<String> graphReadConsistencyLevel;

  /**
   * The write consistency level to use for graph statements. If not specified, it defaults to
   * {@link ConsistencyLevel#LOCAL_ONE}.
   *
   * <p>DSE Graph is able to distinguish between read and write timeouts for the internal storage
   * queries that will be produced by a traversal. Hence the consistency level for reads and writes
   * can be set separately.
   */
  @ConfigItem(name = "write-consistency-level")
  public Optional<String> graphWriteConsistencyLevel;
}
