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
import com.datastax.oss.driver.internal.core.util.concurrent.CompletableFutures;
import com.datastax.oss.quarkus.runtime.api.config.CassandraClientConfig;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.runtime.internal.session.QuarkusCqlSessionBuilder;
import com.typesafe.config.ConfigFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.netty.channel.EventLoopGroup;
import io.quarkus.arc.Unremovable;
import io.quarkus.netty.MainEventLoopGroup;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CassandraClientProducer {

  private static final Logger LOG = LoggerFactory.getLogger(CassandraClientProducer.class);

  private String protocolCompression;
  private Object metricRegistry;
  private String metricsFactoryClass;

  @Produces
  @ApplicationScoped
  @Unremovable
  public QuarkusCqlSessionStageBeanState produceQuarkusCqlSessionStageBeanState() {
    LOG.debug("Producing QuarkusCqlSessionStageBeanState bean");
    return new QuarkusCqlSessionStageBeanState();
  }

  @Produces
  @ApplicationScoped
  @Unremovable
  public CompletionStage<QuarkusCqlSession> produceQuarkusCqlSessionStage(
      CassandraClientConfig config,
      @MainEventLoopGroup EventLoopGroup mainEventLoop,
      QuarkusCqlSessionStageBeanState sessionStageBeanState) {
    LOG.debug(
        "Producing CompletionStage<QuarkusCqlSession> bean, metricRegistry = {}, useQuarkusEventLoop = {}",
        metricRegistry,
        config.cassandraClientInitConfig.useQuarkusEventLoop);
    ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder = createDriverConfigLoaderBuilder();
    configureRuntimeSettings(configLoaderBuilder, config);
    configureMetricsSettings(configLoaderBuilder, config);
    configureProtocolCompression(configLoaderBuilder);
    QuarkusCqlSessionBuilder builder =
        new QuarkusCqlSessionBuilder().withConfigLoader(configLoaderBuilder.build());
    if (metricRegistry != null) {
      LOG.debug("Metric registry = {}", metricRegistry);
      builder.withMetricRegistry(metricRegistry);
    }
    if (config.cassandraClientInitConfig.useQuarkusEventLoop) {
      builder.withQuarkusEventLoop(mainEventLoop);
    }
    if (config.cassandraClientInitConfig.eagerSessionInit) {
      LOG.info("Eagerly initializing Quarkus Cassandra client");
    } else {
      LOG.info("Initializing Quarkus Cassandra client");
    }
    CompletionStage<QuarkusCqlSession> sessionFuture = builder.buildAsync();
    sessionFuture.whenComplete(
        (session, error) -> {
          if (error == null) {
            LOG.info("Quarkus Cassandra client successfully initialized");
          } else {
            LOG.error("Quarkus Cassandra client failed to initialize", error);
          }
        });
    sessionStageBeanState.setProduced();
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
        config.cassandraClientInitConfig.eagerSessionInit);
    if (!config.cassandraClientInitConfig.eagerSessionInit
        && config.cassandraClientInitConfig.eagerSessionInitInfo) {
      LOG.info(
          "Injecting QuarkusCqlSession and setting eager-session-init = false may cause problems if the lazy initialization process "
              + "happens on a thread that is not allowed to block, such as Vert.x thread.");
      LOG.info(
          "Please either set eager-session-init to true, or inject CompletionStage<QuarkusCqlSession> instead, "
              + "or make sure that the lazy initialization process in your application is not happening on a Vert.x thread.");
      LOG.info(
          "Set the config property eager-session-init-info to false to suppress this message.");
    }
    return sessionFuture.toCompletableFuture().get();
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
