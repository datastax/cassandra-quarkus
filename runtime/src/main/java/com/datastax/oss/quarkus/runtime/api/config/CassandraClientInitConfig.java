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

/** This class holds runtime configuration items related to session initialization. */
@ConfigGroup
public class CassandraClientInitConfig {

  /**
   * Whether to eagerly initialize the session at application startup.
   *
   * <p>When true, the {@link com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession
   * QuarkusCqlSession} instance will be created and initialized eagerly during the application
   * startup. This should be fine for most applications.
   *
   * <p>When false, the QuarkusCqlSession instance will be initialized on its first access. This may
   * speed up the application startup, but may also be problematic if the initialization is done on
   * a thread that is not allowed to block, such as a Vert.x event loop thread, since the
   * initialization process is blocking.
   *
   * <p>The default is true.
   */
  @ConfigItem(name = "eager-session-init", defaultValue = "true")
  public boolean eagerSessionInit;

  /**
   * Whether to try to reconnect if the first connection attempt fails.
   *
   * <p>When true, the session will apply its reconnection policy settings and will keep trying to
   * connect until the connection succeeds.
   *
   * <p>When false, any failure to connect during session initialization will throw an error and
   * prevent the application from starting.
   *
   * <p>The default is true.
   */
  @ConfigItem(name = "reconnect-on-init", defaultValue = "true")
  public boolean reconnectOnInit;

  /**
   * Whether to resolve contact points eagerly when initializing the session.
   *
   * <p>When false, addresses are created with {@link
   * java.net.InetSocketAddress#createUnresolved(String, int)}: the host name will be resolved again
   * every time the driver opens a new connection. This is useful for containerized environments
   * where DNS records are more likely to change over time (note that the JVM and OS have their own
   * DNS caching mechanisms, so you might need additional configuration beyond the driver).
   *
   * <p>When true, addresses are created with {@link
   * java.net.InetSocketAddress#InetSocketAddress(String, int)}`: the host name will be resolved the
   * first time, and the driver will use the resolved IP address for all subsequent connection
   * attempts.
   *
   * <p>The default is false.
   */
  @ConfigItem(name = "resolve-contact-points", defaultValue = "false")
  public boolean resolveContactPoints;
}
