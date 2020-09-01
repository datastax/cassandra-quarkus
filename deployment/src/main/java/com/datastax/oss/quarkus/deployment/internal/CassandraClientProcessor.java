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
package com.datastax.oss.quarkus.deployment.internal;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import com.datastax.dse.driver.api.core.auth.ProgrammaticDseGssApiAuthProvider;
import com.datastax.dse.driver.internal.core.auth.DseGssApiAuthProvider;
import com.datastax.dse.driver.internal.core.tracker.MultiplexingRequestTracker;
import com.datastax.oss.driver.api.core.metadata.SafeInitNodeStateListener;
import com.datastax.oss.driver.api.core.ssl.ProgrammaticSslEngineFactory;
import com.datastax.oss.driver.internal.core.addresstranslation.Ec2MultiRegionAddressTranslator;
import com.datastax.oss.driver.internal.core.addresstranslation.PassThroughAddressTranslator;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
import com.datastax.oss.driver.internal.core.auth.ProgrammaticPlainTextAuthProvider;
import com.datastax.oss.driver.internal.core.connection.ConstantReconnectionPolicy;
import com.datastax.oss.driver.internal.core.connection.ExponentialReconnectionPolicy;
import com.datastax.oss.driver.internal.core.loadbalancing.DcInferringLoadBalancingPolicy;
import com.datastax.oss.driver.internal.core.loadbalancing.DefaultLoadBalancingPolicy;
import com.datastax.oss.driver.internal.core.metadata.MetadataManager;
import com.datastax.oss.driver.internal.core.metadata.NoopNodeStateListener;
import com.datastax.oss.driver.internal.core.metadata.schema.NoopSchemaChangeListener;
import com.datastax.oss.driver.internal.core.os.Native;
import com.datastax.oss.driver.internal.core.retry.DefaultRetryPolicy;
import com.datastax.oss.driver.internal.core.session.throttling.ConcurrencyLimitingRequestThrottler;
import com.datastax.oss.driver.internal.core.session.throttling.PassThroughRequestThrottler;
import com.datastax.oss.driver.internal.core.session.throttling.RateLimitingRequestThrottler;
import com.datastax.oss.driver.internal.core.specex.ConstantSpeculativeExecutionPolicy;
import com.datastax.oss.driver.internal.core.specex.NoSpeculativeExecutionPolicy;
import com.datastax.oss.driver.internal.core.ssl.DefaultSslEngineFactory;
import com.datastax.oss.driver.internal.core.ssl.SniSslEngineFactory;
import com.datastax.oss.driver.internal.core.time.AtomicTimestampGenerator;
import com.datastax.oss.driver.internal.core.time.ServerSideTimestampGenerator;
import com.datastax.oss.driver.internal.core.time.ThreadLocalTimestampGenerator;
import com.datastax.oss.driver.internal.core.tracker.NoopRequestTracker;
import com.datastax.oss.driver.internal.core.tracker.RequestLogger;
import com.datastax.oss.quarkus.deployment.api.CassandraClientBuildTimeConfig;
import com.datastax.oss.quarkus.runtime.api.config.CassandraClientConfig;
import com.datastax.oss.quarkus.runtime.internal.metrics.MetricsConfig;
import com.datastax.oss.quarkus.runtime.internal.quarkus.CassandraClientProducer;
import com.datastax.oss.quarkus.runtime.internal.quarkus.CassandraClientRecorder;
import com.datastax.oss.quarkus.runtime.internal.quarkus.CassandraClientStarter;
import com.esri.core.geometry.ogc.OGCGeometry;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.SyntheticBeansRuntimeInitBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerIoRegistryV3d0;
import org.reactivestreams.Publisher;

class CassandraClientProcessor {

  public static final String CASSANDRA_CLIENT = "cassandra-client";

  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(CASSANDRA_CLIENT);
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerDriverUsedClassesForReflection() {
    return Arrays.asList(
        new ReflectiveClassBuildItem(true, true, OGCGeometry.class.getName()),
        new ReflectiveClassBuildItem(true, true, GraphTraversal.class.getName()),
        new ReflectiveClassBuildItem(true, true, TinkerIoRegistryV3d0.class.getName()),
        new ReflectiveClassBuildItem(true, true, JsonParser.class.getName()),
        new ReflectiveClassBuildItem(true, true, ObjectMapper.class.getName()));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerReactiveForReflection() {
    return Collections.singletonList(
        new ReflectiveClassBuildItem(true, true, Publisher.class.getName()));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerRetryPoliciesForReflection() {
    return Collections.singletonList(
        new ReflectiveClassBuildItem(true, true, DefaultRetryPolicy.class.getName()));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerSchemaChangeListenersForReflection() {
    return Collections.singletonList(
        new ReflectiveClassBuildItem(true, true, NoopSchemaChangeListener.class.getName()));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerStateListenersForRefection() {
    return Arrays.asList(
        new ReflectiveClassBuildItem(true, true, NoopNodeStateListener.class.getName()),
        new ReflectiveClassBuildItem(true, true, SafeInitNodeStateListener.class.getName()));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerSpeculativeExecutionPoliciesForReflection() {
    return Arrays.asList(
        new ReflectiveClassBuildItem(true, true, NoSpeculativeExecutionPolicy.class.getName()),
        new ReflectiveClassBuildItem(
            true, true, ConstantSpeculativeExecutionPolicy.class.getName()));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerAddressTranslatorsForReflection() {
    return Arrays.asList(
        new ReflectiveClassBuildItem(true, true, Ec2MultiRegionAddressTranslator.class.getName()),
        new ReflectiveClassBuildItem(true, true, PassThroughAddressTranslator.class.getName()));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerLoadBalancingPoliciesForReflection() {
    return Arrays.asList(
        new ReflectiveClassBuildItem(true, true, DefaultLoadBalancingPolicy.class.getName()),
        new ReflectiveClassBuildItem(true, true, DcInferringLoadBalancingPolicy.class.getName()));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerReconnectionPoliciesForReflection() {
    return Arrays.asList(
        new ReflectiveClassBuildItem(true, true, ExponentialReconnectionPolicy.class.getName()),
        new ReflectiveClassBuildItem(true, true, ConstantReconnectionPolicy.class.getName()));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerRequestThrottlersForReflection() {
    return Arrays.asList(
        new ReflectiveClassBuildItem(true, true, PassThroughRequestThrottler.class.getName()),
        new ReflectiveClassBuildItem(
            true, true, ConcurrencyLimitingRequestThrottler.class.getName()),
        new ReflectiveClassBuildItem(true, true, RateLimitingRequestThrottler.class.getName()));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerRequestTrackersForReflection() {
    return Arrays.asList(
        new ReflectiveClassBuildItem(true, true, NoopRequestTracker.class.getName()),
        new ReflectiveClassBuildItem(true, true, MultiplexingRequestTracker.class.getName()),
        new ReflectiveClassBuildItem(true, true, RequestLogger.class.getName()));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerLz4ForReflection(
      CassandraClientBuildTimeConfig buildTimeConfig) {
    if (buildTimeConfig.protocolCompression.equalsIgnoreCase("lz4")) {
      return Arrays.asList(
          new ReflectiveClassBuildItem(true, true, "net.jpountz.lz4.LZ4Compressor"),
          new ReflectiveClassBuildItem(true, true, "net.jpountz.lz4.LZ4JavaSafeCompressor"),
          new ReflectiveClassBuildItem(true, true, "net.jpountz.lz4.LZ4HCJavaSafeCompressor"),
          new ReflectiveClassBuildItem(true, true, "net.jpountz.lz4.LZ4JavaSafeFastDecompressor"),
          new ReflectiveClassBuildItem(true, true, "net.jpountz.lz4.LZ4JavaSafeSafeDecompressor"),
          new ReflectiveClassBuildItem(true, true, "net.jpountz.lz4.LZ4JavaUnsafeCompressor"),
          new ReflectiveClassBuildItem(true, true, "net.jpountz.lz4.LZ4HCJavaUnsafeCompressor"),
          new ReflectiveClassBuildItem(true, true, "net.jpountz.lz4.LZ4JavaUnsafeFastDecompressor"),
          new ReflectiveClassBuildItem(
              true, true, "net.jpountz.lz4.LZ4JavaUnsafeSafeDecompressor"));
    } else {
      return Collections.emptyList();
    }
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerSSLFactoryForReflection() {
    return Arrays.asList(
        new ReflectiveClassBuildItem(true, true, DefaultSslEngineFactory.class.getName()),
        new ReflectiveClassBuildItem(true, true, ProgrammaticSslEngineFactory.class.getName()),
        new ReflectiveClassBuildItem(true, true, SniSslEngineFactory.class.getName()));
  }

  @BuildStep
  void setupSslSupport(
      BuildProducer<ExtensionSslNativeSupportBuildItem> extensionSslNativeSupport) {
    extensionSslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem(CASSANDRA_CLIENT));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerAuthProviderForReflection() {
    return Arrays.asList(
        new ReflectiveClassBuildItem(true, true, PlainTextAuthProvider.class.getName()),
        new ReflectiveClassBuildItem(true, true, DseGssApiAuthProvider.class.getName()),
        new ReflectiveClassBuildItem(true, true, ProgrammaticPlainTextAuthProvider.class.getName()),
        new ReflectiveClassBuildItem(
            true, true, ProgrammaticDseGssApiAuthProvider.class.getName()));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerTimestampGeneratorsForReflection() {
    return Arrays.asList(
        new ReflectiveClassBuildItem(true, true, AtomicTimestampGenerator.class.getName()),
        new ReflectiveClassBuildItem(true, true, ThreadLocalTimestampGenerator.class.getName()),
        new ReflectiveClassBuildItem(true, true, ServerSideTimestampGenerator.class.getName()));
  }

  @Record(RUNTIME_INIT)
  @BuildStep
  @Consume(SyntheticBeansRuntimeInitBuildItem.class)
  void configureRuntimeProperties(
      CassandraClientRecorder recorder,
      CassandraClientConfig runtimeConfig,
      CassandraClientBuildTimeConfig buildTimeConfig,
      Capabilities capabilities) {
    recorder.configureRuntimeProperties(runtimeConfig);
    configureMetrics(recorder, buildTimeConfig, capabilities);
    recorder.configureCompression(buildTimeConfig.protocolCompression);
    recorder.setInjectedNettyEventLoop(buildTimeConfig.useQuarkusNettyEventLoop);
  }

  private void configureMetrics(
      CassandraClientRecorder recorder,
      CassandraClientBuildTimeConfig buildTimeConfig,
      Capabilities capabilities) {
    if (buildTimeConfig.metricsEnabled) {
      recorder.configureMetrics(
          new MetricsConfig(
              buildTimeConfig.metricsNodeEnabled, buildTimeConfig.metricsSessionEnabled, true));
    } else {
      recorder.configureMetrics(new MetricsConfig(Optional.empty(), Optional.empty(), false));
    }

    if (buildTimeConfig.metricsEnabled && capabilities.isCapabilityPresent(Capabilities.METRICS)) {
      recorder.setInjectedMetricRegistry();
    } else {
      recorder.setNoopMetricRegistry();
    }
  }

  @BuildStep
  AdditionalBeanBuildItem cassandraClientProducer() {
    return AdditionalBeanBuildItem.unremovableOf(CassandraClientProducer.class);
  }

  @BuildStep
  AdditionalBeanBuildItem cassandraClientStarter() {
    return AdditionalBeanBuildItem.builder().addBeanClass(CassandraClientStarter.class).build();
  }

  @BuildStep
  @Record(value = RUNTIME_INIT)
  CassandraClientBuildItem cassandraClient(
      CassandraClientRecorder recorder,
      ShutdownContextBuildItem shutdown,
      BeanContainerBuildItem beanContainer) {
    return new CassandraClientBuildItem(recorder.buildClient(shutdown, beanContainer.getValue()));
  }

  @BuildStep
  HealthBuildItem addHealthCheck(CassandraClientBuildTimeConfig buildTimeConfig) {
    return new HealthBuildItem(
        "com.datastax.oss.quarkus.runtime.internal.health.CassandraHealthCheck",
        buildTimeConfig.healthEnabled,
        "cassandra");
  }

  /**
   * MetadataManager must be initialized at runtime because it uses Inet4Socket address that cannot
   * be initialized at the deployment time because of: No instances of java.net.Inet4Address are
   * allowed in the image heap as this class should be initialized at image runtime.
   *
   * @return RuntimeInitializedClassBuildItem of {@link MetadataManager} that initialization will be
   *     deferred to runtime.
   */
  @BuildStep
  RuntimeInitializedClassBuildItem runtimeMetadataManager() {
    return new RuntimeInitializedClassBuildItem(MetadataManager.class.getCanonicalName());
  }

  @BuildStep
  NativeImageResourceBuildItem referenceConf() {
    return new NativeImageResourceBuildItem("reference.conf");
  }

  @BuildStep
  RuntimeInitializedClassBuildItem runtimeNative() {
    return new RuntimeInitializedClassBuildItem(Native.class.getCanonicalName());
  }
}
