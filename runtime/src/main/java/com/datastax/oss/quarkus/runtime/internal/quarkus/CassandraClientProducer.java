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
package com.datastax.oss.quarkus.runtime.internal.quarkus;

import com.datastax.dse.driver.api.core.config.DseDriverOption;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfig;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader;
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.internal.core.metrics.TaggingMetricIdGenerator;
import com.datastax.oss.driver.internal.core.util.concurrent.CompletableFutures;
import com.datastax.oss.quarkus.runtime.api.config.CassandraClientConfig;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.runtime.internal.session.QuarkusCqlSessionBuilder;
import com.typesafe.config.ConfigFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.MultithreadEventExecutorGroup;
import io.quarkus.arc.Unremovable;
import io.quarkus.netty.MainEventLoopGroup;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CassandraClientProducer {

  private static final Logger LOG = LoggerFactory.getLogger(CassandraClientProducer.class);

  private static final int MIN_EVENT_LOOP_GROUP_SIZE = 4;

  private final AtomicBoolean produced = new AtomicBoolean(false);

  // injected by CassandraClientRecorder
  private String protocolCompression;
  private Object metricRegistry;
  private String metricsFactoryClass;
  private final List<String> requestTrackers = new ArrayList<>();
  private final List<String> nodeStateListeners = new ArrayList<>();
  private final List<String> schemaChangeListeners = new ArrayList<>();

  @Produces
  @ApplicationScoped
  @Unremovable
  public CompletionStage<QuarkusCqlSession> produceQuarkusCqlSessionStage(
      CassandraClientConfig config, @MainEventLoopGroup EventLoopGroup mainEventLoop) {
    LOG.debug(
        "Producing CompletionStage<QuarkusCqlSession> bean, metricRegistry = {}, useQuarkusEventLoop = {}",
        metricRegistry,
        config.cassandraClientInitConfig.useQuarkusEventLoop);
    ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder = createDriverConfigLoaderBuilder();
    configureRuntimeSettings(configLoaderBuilder, config);
    configureMetricsSettings(configLoaderBuilder, config);
    configureProtocolCompression(configLoaderBuilder);
    configureListeners(configLoaderBuilder);
    QuarkusCqlSessionBuilder builder =
        new QuarkusCqlSessionBuilder()
            .withConfigLoader(configLoaderBuilder.build())
            .withClassLoader(Thread.currentThread().getContextClassLoader());
    if (metricRegistry != null) {
      LOG.debug("Metric registry = {}", metricRegistry);
      builder.withMetricRegistry(metricRegistry);
    }
    if (config.cassandraClientInitConfig.useQuarkusEventLoop) {
      if (mainEventLoop instanceof MultithreadEventExecutorGroup) {
        // Check event loop group size. The default in Quarkus is 2 * cores, which is usually fine.
        // https://quarkus.io/guides/vertx-reference#quarkus-vertx-core_quarkus.vertx.event-loops-pool-size
        int executors = ((MultithreadEventExecutorGroup) mainEventLoop).executorCount();
        if (executors < MIN_EVENT_LOOP_GROUP_SIZE) {
          LOG.warn(
              "Main event loop pool size is too small: {}; the Quarkus Cassandra session might experience deadlocks.",
              executors);
          LOG.warn(
              "Please either set the quarkus.vertx.event-loops-pool-size property to a value >= {}, or "
                  + "set the quarkus.cassandra.use-quarkus-event-loop property to false.",
              MIN_EVENT_LOOP_GROUP_SIZE);
        }
      }
      builder.withQuarkusEventLoop(mainEventLoop);
    }
    CompletionStage<QuarkusCqlSession> sessionFuture = builder.buildAsync();
    produced.set(true);
    return sessionFuture;
  }

  @Produces
  @ApplicationScoped
  @Unremovable
  public QuarkusCqlSession produceQuarkusCqlSession(
      CompletionStage<QuarkusCqlSession> sessionFuture, CassandraClientConfig config)
      throws ExecutionException, InterruptedException {
    LOG.debug(
        "Producing QuarkusCqlSession bean, eagerSessionInit = {}",
        config.cassandraClientInitConfig.eagerInit);
    if (!config.cassandraClientInitConfig.eagerInit
        && config.cassandraClientInitConfig.printEagerInitInfo) {
      LOG.info(
          "Injecting QuarkusCqlSession and setting quarkus.cassandra.init.eager-init = false "
              + "may cause problems if the lazy initialization process "
              + "happens on a thread that is not allowed to block, such as Vert.x thread.");
      LOG.info(
          "Please either set quarkus.cassandra.init.eager-init = true, "
              + "or inject CompletionStage<QuarkusCqlSession> instead, "
              + "or make sure that the lazy initialization process "
              + " is not happening on a Vert.x thread.");
      LOG.info(
          "Set the config property quarkus.cassandra.init.print-eager-init-info = false "
              + "to suppress this message.");
    }
    return sessionFuture.toCompletableFuture().get();
  }

  @Produces
  @ApplicationScoped
  @Unremovable
  public Uni<QuarkusCqlSession> produceQuarkusCqlSessionUni(
      CompletionStage<QuarkusCqlSession> sessionFuture) {
    LOG.debug("Producing Uni<QuarkusCqlSession>");
    return Uni.createFrom().completionStage(sessionFuture);
  }

  public void setMetricsFactoryClassName(String metricsFactoryClass) {
    this.metricsFactoryClass = metricsFactoryClass;
  }

  public void setMetricRegistry(Object metricRegistry) {
    this.metricRegistry = metricRegistry;
  }

  public void setProtocolCompression(String protocolCompression) {
    this.protocolCompression = protocolCompression;
  }

  public void addRequestTrackerClass(String clz) {
    this.requestTrackers.add(clz);
  }

  public void addSchemaChangeListenerClass(String clz) {
    this.schemaChangeListeners.add(clz);
  }

  public void addNodeStateListenerClass(String clz) {
    this.nodeStateListeners.add(clz);
  }

  private ProgrammaticDriverConfigLoaderBuilder createDriverConfigLoaderBuilder() {
    return new DefaultProgrammaticDriverConfigLoaderBuilder(
        () ->
            // The fallback supplier specified here is similar to the default
            // one, except that we don't accept application.properties
            // because it's used by Quarkus.
            ConfigFactory.parseResources("application.conf")
                .withFallback(ConfigFactory.parseResources("application.json"))
                .withFallback(ConfigFactory.defaultReference(CqlSession.class.getClassLoader())),
        DefaultDriverConfigLoader.DEFAULT_ROOT_PATH) {
      @NonNull
      @Override
      public DriverConfigLoader build() {
        return new NonReloadableDriverConfigLoader(super.build());
      }
    };
  }

  private void configureProtocolCompression(
      ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder) {
    configLoaderBuilder.withString(DefaultDriverOption.PROTOCOL_COMPRESSION, protocolCompression);
  }

  private void configureListeners(ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder) {
    if (!requestTrackers.isEmpty()) {
      configLoaderBuilder.withStringList(
          DefaultDriverOption.REQUEST_TRACKER_CLASSES, requestTrackers);
    }
    if (!schemaChangeListeners.isEmpty()) {
      configLoaderBuilder.withStringList(
          DefaultDriverOption.METADATA_SCHEMA_CHANGE_LISTENER_CLASSES, schemaChangeListeners);
    }
    if (!nodeStateListeners.isEmpty()) {
      configLoaderBuilder.withStringList(
          DefaultDriverOption.METADATA_NODE_STATE_LISTENER_CLASSES, nodeStateListeners);
    }
  }

  private void configureMetricsSettings(
      ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder, CassandraClientConfig config) {
    if (metricRegistry != null && metricsFactoryClass != null) {
      List<String> enabledNodeMetrics =
          config.cassandraClientMetricsConfig.enabledNodeMetrics.orElse(Collections.emptyList());
      List<String> enabledSessionMetrics =
          config.cassandraClientMetricsConfig.enabledSessionMetrics.orElse(Collections.emptyList());
      if (checkMetricsPresent(enabledNodeMetrics, enabledSessionMetrics)) {
        configLoaderBuilder.withString(
            DefaultDriverOption.METRICS_FACTORY_CLASS, metricsFactoryClass);
        configLoaderBuilder.withString(
            DefaultDriverOption.METRICS_ID_GENERATOR_CLASS,
            TaggingMetricIdGenerator.class.getName());
        configLoaderBuilder.withString(
            DefaultDriverOption.METRICS_ID_GENERATOR_PREFIX,
            config.cassandraClientMetricsConfig.prefix);
        configLoaderBuilder.withStringList(
            DefaultDriverOption.METRICS_NODE_ENABLED, enabledNodeMetrics);
        configLoaderBuilder.withStringList(
            DefaultDriverOption.METRICS_SESSION_ENABLED, enabledSessionMetrics);
      }
    }
  }

  private boolean checkMetricsPresent(
      List<String> enabledNodeMetrics, List<String> enabledSessionMetrics) {
    if (enabledNodeMetrics.isEmpty() && enabledSessionMetrics.isEmpty()) {
      LOG.warn(
          "Metrics were enabled in the configuration, but no session-level or node-level metrics were enabled; "
              + "forcibly disabling metrics. Make to sure enable at least one metric to track using the "
              + "cassandra.metrics.session.enabled or cassandra.metrics.node.enabled properties.");
      return false;
    }
    return true;
  }

  private void configureRuntimeSettings(
      ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder, CassandraClientConfig config) {
    // connection settings
    config.cassandraClientConnectionConfig.contactPoints.ifPresent(
        v -> configLoaderBuilder.withStringList(DefaultDriverOption.CONTACT_POINTS, v));
    config.cassandraClientConnectionConfig.localDatacenter.ifPresent(
        v ->
            configLoaderBuilder.withString(DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER, v));
    config.cassandraClientConnectionConfig.keyspace.ifPresent(
        v -> configLoaderBuilder.withString(DefaultDriverOption.SESSION_KEYSPACE, v));
    // cloud settings
    config.cassandraClientCloudConfig.secureConnectBundle.ifPresent(
        v ->
            configLoaderBuilder.withString(
                DefaultDriverOption.CLOUD_SECURE_CONNECT_BUNDLE, v.toAbsolutePath().toString()));
    // init settings
    configLoaderBuilder.withBoolean(
        DefaultDriverOption.RESOLVE_CONTACT_POINTS,
        config.cassandraClientInitConfig.resolveContactPoints);
    configLoaderBuilder.withBoolean(
        DefaultDriverOption.RECONNECT_ON_INIT, config.cassandraClientInitConfig.reconnectOnInit);
    // request settings
    config.cassandraClientRequestConfig.requestTimeout.ifPresent(
        v -> configLoaderBuilder.withDuration(DefaultDriverOption.REQUEST_TIMEOUT, v));
    config.cassandraClientRequestConfig.consistencyLevel.ifPresent(
        v -> configLoaderBuilder.withString(DefaultDriverOption.REQUEST_CONSISTENCY, v));
    config.cassandraClientRequestConfig.serialConsistencyLevel.ifPresent(
        v -> configLoaderBuilder.withString(DefaultDriverOption.REQUEST_SERIAL_CONSISTENCY, v));
    config.cassandraClientRequestConfig.pageSize.ifPresent(
        v -> configLoaderBuilder.withInt(DefaultDriverOption.REQUEST_PAGE_SIZE, v));
    config.cassandraClientRequestConfig.defaultIdempotence.ifPresent(
        v -> configLoaderBuilder.withBoolean(DefaultDriverOption.REQUEST_DEFAULT_IDEMPOTENCE, v));
    // auth settings
    if (config.cassandraClientAuthConfig.username.isPresent()
        && config.cassandraClientAuthConfig.password.isPresent()) {
      configLoaderBuilder
          .withClass(DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class)
          .withString(
              DefaultDriverOption.AUTH_PROVIDER_USER_NAME,
              config.cassandraClientAuthConfig.username.get())
          .withString(
              DefaultDriverOption.AUTH_PROVIDER_PASSWORD,
              config.cassandraClientAuthConfig.password.get());
    }
    // graph settings
    config.cassandraClientGraphConfig.graphName.ifPresent(
        v -> configLoaderBuilder.withString(DseDriverOption.GRAPH_NAME, v));
    config.cassandraClientGraphConfig.graphReadConsistencyLevel.ifPresent(
        v -> configLoaderBuilder.withString(DseDriverOption.GRAPH_READ_CONSISTENCY_LEVEL, v));
    config.cassandraClientGraphConfig.graphWriteConsistencyLevel.ifPresent(
        v -> configLoaderBuilder.withString(DseDriverOption.GRAPH_WRITE_CONSISTENCY_LEVEL, v));
    config.cassandraClientGraphConfig.graphRequestTimeout.ifPresent(
        v -> configLoaderBuilder.withDuration(DseDriverOption.GRAPH_TIMEOUT, v));
  }

  public boolean isProduced() {
    return produced.get();
  }

  private static class NonReloadableDriverConfigLoader implements DriverConfigLoader {

    private final DriverConfigLoader delegate;

    public NonReloadableDriverConfigLoader(DriverConfigLoader delegate) {
      this.delegate = delegate;
    }

    @NonNull
    @Override
    public DriverConfig getInitialConfig() {
      return delegate.getInitialConfig();
    }

    @Override
    public void onDriverInit(@NonNull DriverContext context) {
      delegate.onDriverInit(context);
    }

    @NonNull
    @Override
    public CompletionStage<Boolean> reload() {
      return CompletableFutures.failedFuture(
          new UnsupportedOperationException("reload not supported"));
    }

    @Override
    public boolean supportsReloading() {
      return false;
    }

    @Override
    public void close() {
      delegate.close();
    }
  }
}
