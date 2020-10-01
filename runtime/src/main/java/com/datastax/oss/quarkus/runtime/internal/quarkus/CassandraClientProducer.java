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
import com.datastax.oss.quarkus.runtime.internal.metrics.MetricsConfig;
import com.datastax.oss.quarkus.runtime.internal.session.QuarkusCqlSessionBuilder;
import com.typesafe.config.ConfigFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.netty.channel.EventLoopGroup;
import io.quarkus.arc.Unremovable;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CassandraClientProducer {
  private static final Logger LOG = LoggerFactory.getLogger(CassandraClientProducer.class);

  private CassandraClientConfig config;
  private MetricsConfig metricsConfig;
  private MetricRegistry metricRegistry;
  private String protocolCompression;
  private EventLoopGroup mainEventLoop;

  @Produces
  @ApplicationScoped
  @Unremovable
  public QuarkusCqlSessionState quarkusCqlSessionState() {
    return QuarkusCqlSessionState.notInitialized();
  }

  @Produces
  @ApplicationScoped
  @Unremovable
  public CompletionStage<QuarkusCqlSession> createCompletionStageOfCassandraClient(
      QuarkusCqlSessionState quarkusCqlSessionState) {
    LOG.trace("Produce CompletionStage<QuarkusCqlSession> bean.");
    ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder = createDriverConfigLoaderBuilder();
    configureRuntimeSettings(configLoaderBuilder);
    configureMetricsSettings(configLoaderBuilder);
    configureProtocolCompression(configLoaderBuilder);
    QuarkusCqlSessionBuilder builder =
        new QuarkusCqlSessionBuilder()
            .withMetricRegistry(metricRegistry)
            .withQuarkusEventLoop(mainEventLoop)
            .withConfigLoader(configLoaderBuilder.build());
    return builder
        .buildAsync()
        .whenComplete(
            (res, ex) -> {
              if (ex == null) {
                LOG.debug("Setting the QuarkusCqlSessionState to initialized.");
                quarkusCqlSessionState.setInitialized();
              }
            });
  }

  @Produces
  @ApplicationScoped
  @Unremovable
  public QuarkusCqlSession createCassandraClient(CompletionStage<QuarkusCqlSession> sessionFuture) {
    LOG.trace("Produce QuarkusCqlSession bean.");
    if (!config.cassandraClientInitConfig.eagerSessionInit) {
      LOG.info(
          "Injecting QuarkusCqlSession and setting eager-session-init = false may cause deadlock on the Vert.x thread. "
              + "Please set it to true, inject only CompletionStage<QuarkusCqlSession>, "
              + "or assert that initializing of the QuarkusCqlSession in your Application is happening not on the Vert.x event loop.");
    }
    return CompletableFutures.getUninterruptibly(sessionFuture);
  }

  public void setCassandraClientConfig(CassandraClientConfig config) {
    this.config = config;
  }

  public void setMetricsConfig(MetricsConfig metricsConfig) {
    this.metricsConfig = metricsConfig;
  }

  public void setMetricRegistry(MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
  }

  public void setProtocolCompression(String protocolCompression) {
    this.protocolCompression = protocolCompression;
  }

  public void setMainEventLoop(EventLoopGroup mainEventLoop) {
    this.mainEventLoop = mainEventLoop;
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

  public CassandraClientConfig getCassandraClientConfig() {
    return config;
  }

  public MetricsConfig getMetricsConfig() {
    return metricsConfig;
  }

  public MetricRegistry getMetricRegistry() {
    return metricRegistry;
  }

  public String getProtocolCompression() {
    return protocolCompression;
  }

  public EventLoopGroup getMainEventLoop() {
    return mainEventLoop;
  }

  private void configureProtocolCompression(
      ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder) {
    configLoaderBuilder.withString(DefaultDriverOption.PROTOCOL_COMPRESSION, protocolCompression);
  }

  private void configureMetricsSettings(ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder) {
    configLoaderBuilder.withStringList(
        DefaultDriverOption.METRICS_NODE_ENABLED, metricsConfig.metricsNodeEnabled);
    configLoaderBuilder.withStringList(
        DefaultDriverOption.METRICS_SESSION_ENABLED, metricsConfig.metricsSessionEnabled);
  }

  private void configureRuntimeSettings(ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder) {
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
