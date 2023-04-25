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

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import java.util.Optional;
import java.util.OptionalInt;

/** This class provides dev services configuration for Cassandra. */
@ConfigGroup
public class CassandraDevServicesBuildTimeConfig {

  /**
   * If Dev Services for Cassandra has been explicitly enabled or disabled. Dev Services are
   * generally enabled by default, unless there is an existing configuration present. For Cassandra,
   * Dev Services starts an instance unless {@code quarkus.cassnadra.contact-points} has been
   * configured.
   */
  @ConfigItem public Optional<Boolean> enabled = Optional.empty();

  /**
   * Optional fixed port the dev service will listen to.
   *
   * <p>If not defined, the port will be chosen randomly.
   */
  @ConfigItem public OptionalInt port;

  /** The image to use. */
  @ConfigItem(defaultValue = "cassandra:latest")
  public String imageName;

  /**
   * Indicates if the Cassandra instance managed by Quarkus Dev Services is shared. When shared,
   * Quarkus looks for running containers using label-based service discovery. If a matching
   * container is found, it is used, and so a second one is not started. Otherwise, Dev Services for
   * Cassandra starts a new container.
   *
   * <p>The discovery uses the {@code quarkus-dev-service-cassandra} label. The value is configured
   * using the {@code service-name} property.
   *
   * <p>Container sharing is only used in dev mode.
   */
  @ConfigItem(defaultValue = "true")
  public boolean shared;

  /**
   * The value of the {@code quarkus-dev-service-cassandra} label attached to the started container.
   * This property is used when {@code shared} is set to {@code true}. In this case, before starting
   * a container, Dev Services for Cassandra looks for a container with the {@code
   * quarkus-dev-service-cassandra} label set to the configured value. If found, it will use this
   * container instead of starting a new one. Otherwise, it starts a new container with the {@code
   * quarkus-dev-service-cassandra} label set to the specified value.
   *
   * <p>This property is used when you need multiple shared Cassandra.
   */
  @ConfigItem(defaultValue = "cassandra")
  public String serviceName;

  /** Init script (starting with creation of keyspace(s)) for Cassandra */
  @ConfigItem(name = "init-script")
  public Optional<String> initScript;
}
