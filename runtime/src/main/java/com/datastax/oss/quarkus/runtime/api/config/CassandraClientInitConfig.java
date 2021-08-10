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
import java.time.Duration;

/** This class holds runtime configuration items related to session initialization. */
@ConfigGroup
public class CassandraClientInitConfig {

  /**
   * Whether to eagerly initialize the Cassandra client at application startup. This includes the
   * session and all mappers and DAOs.
   *
   * <p>When false, the {@link com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession
   * QuarkusCqlSession} instance will be initialized on its first access. This is usually fine for
   * most applications and also speeds up the application startup time, but may be problematic if
   * the initialization is done on a thread that is not allowed to block, such as a Vert.x event
   * loop thread, since the initialization process is blocking and can take quite some time to
   * finish.
   *
   * <p>When true, the QuarkusCqlSession instance will be created and initialized eagerly during the
   * application startup phase, on the Quarkus main thread. This slows down the startup time, but
   * can be helpful to avoid performing blocking operations on threads that cannot block.
   *
   * <p>The default is false.
   */
  @ConfigItem(name = "eager-init", defaultValue = "false")
  public boolean eagerInit;

  /**
   * How long to wait for the Cassandra client to initialize at application startup. Ignored when
   * {@link #eagerInit} is false.
   *
   * <p>If the client fails to initialize during this period, for example because {@link
   * #reconnectOnInit} is true but the cluster is unreachable, the application will resume its
   * startup process in order to avoid blocking the application completely, but a warning will be
   * logged. This usually means that the Cassandra connection properties are wrong, or the cluster
   * is down.
   *
   * <p>The default is 10 seconds.
   */
  @ConfigItem(name = "eager-init-timeout", defaultValue = "PT10S")
  public Duration eagerInitTimeout;

  /**
   * Whether to log an informational message explaining how to best use eager initialization.
   *
   * <p>If {@linkplain #eagerInit eager initialization at startup} is disabled, then users should
   * not inject {@link com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession
   * QuarkusCqlSession} instances directly in their application, but rather {@code
   * CompletionStage<QuarkusCqlSession>} or {@code Uni<QuarkusCqlSession>} instances, to avoid
   * performing blocking operations on threads that are not allowed to block, such as Vert.x
   * application threads. An informational message will be logged if this rule in infringed, unless
   * this option is set to false.
   */
  @ConfigItem(name = "print-eager-init-info", defaultValue = "true")
  public boolean printEagerInitInfo;

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

  /**
   * Whether the DataStax Java driver should use the main Netty event loop group provided by Quarkus
   * and Vert.x.
   *
   * <p>The driver needs two Netty multi-threaded event loop groups: one (generally large) for I/O
   * tasks (sending requests and processing responses); and another one (generally small) for
   * administrative tasks (metadata refreshes, schema agreement checks, and processing of server
   * events).
   *
   * <p>When set to true, the driver will use the main event loop group provided by Quarkus and
   * Vert.x <em>for both I/O tasks and administrative tasks</em>. When set to false, the driver will
   * instead create and manage its own event loops groups, backed by internal thread pools.
   *
   * <p>The default is true.
   *
   * <p>Make sure that the Quarkus/Vert.x main event loop is configured with a pool size of at least
   * 4 threads. <em>Using fewer threads might cause the session to experience deadlocks</em>. By
   * default, Quarkus allocates a pool size of twice the number of available cores, which is
   * generally fine; this value can however be manually changed using the {@code
   * quarkus.vertx.event-loops-pool-size} configuration property.
   *
   * @see <a href="https://quarkus.io/guides/vertx-reference">Quarkus Vert.x reference guide</a>
   */
  @ConfigItem(name = "use-quarkus-event-loop", defaultValue = "true")
  public boolean useQuarkusEventLoop;
}
