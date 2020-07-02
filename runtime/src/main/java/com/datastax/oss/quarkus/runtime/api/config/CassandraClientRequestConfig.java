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
import java.util.OptionalInt;

/** This class holds runtime configuration items related to connection settings. */
@ConfigGroup
public class CassandraClientRequestConfig {

  /**
   * How long the driver waits for a request to complete. If not specified, it defaults to 2
   * seconds.
   *
   * <p>This is a global limit on the duration of a query, including any internal retries the driver
   * might do.
   *
   * <p>By default, this value is set pretty high to ensure that DDL queries don't time out, in
   * order to provide the best experience for new users trying the driver with the out-of-the-box
   * configuration.
   *
   * <p>For any serious deployment, we recommend that you use separate configuration profiles for
   * DDL and DML; you can then set the DML timeout much lower (down to a few milliseconds if
   * needed).
   */
  @ConfigItem(name = "timeout")
  public Optional<Duration> requestTimeout;

  /**
   * The default consistency level to use. If not specified, it defaults to {@link
   * ConsistencyLevel#LOCAL_ONE}.
   */
  @ConfigItem(name = "consistency-level")
  public Optional<String> consistencyLevel;

  /**
   * The default serial consistency level to use. If not specified, it defaults to {@link
   * ConsistencyLevel#SERIAL}.
   */
  @ConfigItem(name = "serial-consistency-level")
  public Optional<String> serialConsistencyLevel;

  /** The default page size to use. If not specified, it defaults to 5000. */
  @ConfigItem(name = "page-size")
  public OptionalInt pageSize;

  /** The default idempotence of a request. If not specified, it defaults to false. */
  @ConfigItem(name = "default-idempotence")
  public Optional<Boolean> defaultIdempotence;
}
