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
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import com.datastax.oss.driver.internal.core.metadata.MetadataManager;
import com.datastax.oss.driver.internal.core.metrics.DefaultMetricsFactory;
import com.datastax.oss.driver.internal.core.metrics.TaggingMetricIdGenerator;
import com.datastax.oss.driver.internal.core.os.Native;
import com.datastax.oss.quarkus.deployment.api.CassandraClientBuildTimeConfig;
import com.datastax.oss.quarkus.runtime.internal.quarkus.CassandraClientProducer;
import com.datastax.oss.quarkus.runtime.internal.quarkus.CassandraClientRecorder;
import com.datastax.oss.quarkus.runtime.internal.quarkus.CassandraClientStarter;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.SyntheticBeansRuntimeInitBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.metrics.MetricsCapabilityBuildItem;
import io.quarkus.runtime.metrics.MetricsFactory;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jboss.jandex.DotName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CassandraClientProcessor {

  public static final String CASSANDRA_CLIENT = "cassandra-client";

  private static final Logger LOG = LoggerFactory.getLogger(CassandraClientProcessor.class);

  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(CASSANDRA_CLIENT);
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerGraphForReflection() {
    return Arrays.asList(
        // Required for the driver DependencyCheck mechanism
        new ReflectiveClassBuildItem(
            true, true, "org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal"),
        // Should be initialized at build time:
        new ReflectiveClassBuildItem(
            true, true, "org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerIoRegistryV3d0"),
        new ReflectiveClassBuildItem(
            true, true, "org.apache.tinkerpop.shaded.jackson.databind.deser.std.StdDeserializer"),
        // Required by Tinkerpop:
        // TODO check if this is really all that is instantiated by reflection
        new ReflectiveClassBuildItem(true, true, "org.apache.tinkerpop.gremlin.structure.Graph"),
        new ReflectiveClassBuildItem(
            true, true, "org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph"),
        new ReflectiveClassBuildItem(
            true, true, "org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph"),
        new ReflectiveClassBuildItem(
            true,
            true,
            "org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource"));
  }

  @BuildStep
  ReflectiveClassBuildItem registerGeometryForReflection() {
    // Required for the driver DependencyCheck mechanism
    return new ReflectiveClassBuildItem(
        false, false, false, false, "com.esri.core.geometry.ogc.OGCGeometry");
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerJsonForReflection() {
    // Required for the driver DependencyCheck mechanism
    return Arrays.asList(
        new ReflectiveClassBuildItem(
            false, false, false, false, "com.fasterxml.jackson.core.JsonParser"),
        new ReflectiveClassBuildItem(
            false, false, false, false, "com.fasterxml.jackson.databind.ObjectMapper"));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerReactiveForReflection() {
    // Required for the driver DependencyCheck mechanism
    return Collections.singletonList(
        new ReflectiveClassBuildItem(false, false, false, false, "org.reactivestreams.Publisher"));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerLz4ForReflection(
      CassandraClientBuildTimeConfig buildTimeConfig) {
    if (buildTimeConfig.protocolCompression.equalsIgnoreCase("lz4")) {
      return Arrays.asList(
          new ReflectiveClassBuildItem(true, false, true, "net.jpountz.lz4.LZ4Compressor"),
          new ReflectiveClassBuildItem(true, false, true, "net.jpountz.lz4.LZ4JavaSafeCompressor"),
          new ReflectiveClassBuildItem(
              true, false, true, "net.jpountz.lz4.LZ4HCJavaSafeCompressor"),
          new ReflectiveClassBuildItem(
              true, false, true, "net.jpountz.lz4.LZ4JavaSafeFastDecompressor"),
          new ReflectiveClassBuildItem(
              true, false, true, "net.jpountz.lz4.LZ4JavaSafeSafeDecompressor"),
          new ReflectiveClassBuildItem(
              true, false, true, "net.jpountz.lz4.LZ4JavaUnsafeCompressor"),
          new ReflectiveClassBuildItem(
              true, false, true, "net.jpountz.lz4.LZ4HCJavaUnsafeCompressor"),
          new ReflectiveClassBuildItem(
              true, false, true, "net.jpountz.lz4.LZ4JavaUnsafeFastDecompressor"),
          new ReflectiveClassBuildItem(
              true, false, true, "net.jpountz.lz4.LZ4JavaUnsafeSafeDecompressor"));
    } else {
      return Collections.emptyList();
    }
  }

  @Record(STATIC_INIT)
  @BuildStep
  List<ReflectiveClassBuildItem> registerRequestTrackersForReflection(
      CassandraClientBuildTimeConfig buildTimeConfig,
      CassandraClientRecorder recorder,
      BeanContainerBuildItem beanContainer) {
    return buildTimeConfig
        .requestTrackers
        .map(
            classes ->
                classes.stream()
                    .map(
                        clz -> {
                          recorder.addRequestTrackerClass(clz);
                          return new ReflectiveClassBuildItem(true, false, false, clz);
                        })
                    .collect(Collectors.toList()))
        .orElse(Collections.emptyList());
  }

  @Record(STATIC_INIT)
  @BuildStep
  List<ReflectiveClassBuildItem> registerNodeStateListenersForReflection(
      CassandraClientBuildTimeConfig buildTimeConfig,
      CassandraClientRecorder recorder,
      BeanContainerBuildItem beanContainer) {
    return buildTimeConfig
        .nodeStateListeners
        .map(
            classes ->
                classes.stream()
                    .map(
                        clz -> {
                          recorder.addNodeStateListenerClass(clz);
                          return new ReflectiveClassBuildItem(true, false, false, clz);
                        })
                    .collect(Collectors.toList()))
        .orElse(Collections.emptyList());
  }

  @Record(STATIC_INIT)
  @BuildStep
  List<ReflectiveClassBuildItem> registerSchemaChangeListenersForReflection(
      CassandraClientBuildTimeConfig buildTimeConfig,
      CassandraClientRecorder recorder,
      BeanContainerBuildItem beanContainer) {
    return buildTimeConfig
        .schemaChangeListeners
        .map(
            classes ->
                classes.stream()
                    .map(
                        clz -> {
                          recorder.addSchemaChangeListenerClass(clz);
                          return new ReflectiveClassBuildItem(true, false, false, clz);
                        })
                    .collect(Collectors.toList()))
        .orElse(Collections.emptyList());
  }

  @BuildStep
  void setupSslSupport(
      BuildProducer<ExtensionSslNativeSupportBuildItem> extensionSslNativeSupport) {
    extensionSslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem(CASSANDRA_CLIENT));
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerMetricsFactoriesForReflection(
      CassandraClientBuildTimeConfig buildTimeConfig,
      Optional<MetricsCapabilityBuildItem> metricsCapability) {
    if (buildTimeConfig.metricsEnabled && metricsCapability.isPresent()) {
      MetricsCapabilityBuildItem metricsCapabilityItem = metricsCapability.get();
      if (metricsCapabilityItem.metricsSupported(MetricsFactory.MICROMETER)) {
        return Arrays.asList(
            new ReflectiveClassBuildItem(
                true,
                true,
                "com.datastax.oss.driver.internal.metrics.micrometer.MicrometerMetricsFactory"),
            new ReflectiveClassBuildItem(true, true, TaggingMetricIdGenerator.class));
      } else if (metricsCapabilityItem.metricsSupported(MetricsFactory.MP_METRICS)) {
        return Arrays.asList(
            new ReflectiveClassBuildItem(
                true,
                true,
                "com.datastax.oss.driver.internal.metrics.microprofile.MicroProfileMetricsFactory"),
            new ReflectiveClassBuildItem(true, true, TaggingMetricIdGenerator.class));
      }
    }
    return Collections.singletonList(
        new ReflectiveClassBuildItem(true, true, DefaultMetricsFactory.class));
  }

  @BuildStep
  UnremovableBeanBuildItem registerMetricsRegistry(
      Optional<MetricsCapabilityBuildItem> metricsCapability) {
    if (metricsCapability.isPresent()) {
      MetricsCapabilityBuildItem metricsCapabilityItem = metricsCapability.get();
      if (metricsCapabilityItem.metricsSupported(MetricsFactory.MICROMETER)) {
        return UnremovableBeanBuildItem.beanTypes(
            DotName.createSimple("io.micrometer.core.instrument.MeterRegistry"));
      } else if (metricsCapabilityItem.metricsSupported(MetricsFactory.MP_METRICS)) {
        return UnremovableBeanBuildItem.targetWithAnnotation(
            DotName.createSimple("org.eclipse.microprofile.metrics.annotation.RegistryType"));
      }
    }
    return null;
  }

  @Record(STATIC_INIT)
  @BuildStep
  void configureMetrics(
      CassandraClientRecorder recorder,
      CassandraClientBuildTimeConfig buildTimeConfig,
      Optional<MetricsCapabilityBuildItem> metricsCapability,
      BeanContainerBuildItem beanContainer) {
    if (buildTimeConfig.metricsEnabled) {
      if (metricsCapability.isPresent()) {
        MetricsCapabilityBuildItem metricsCapabilityItem = metricsCapability.get();
        if (metricsCapabilityItem.metricsSupported(MetricsFactory.MICROMETER)) {
          if (checkMicrometerMetricsFactoryPresent()) {
            recorder.configureMicrometerMetrics();
          } else {
            LOG.warn(
                "Micrometer metrics were enabled by configuration, but MicrometerMetricsFactory was not found.");
            LOG.warn(
                "Make sure to include a dependency to the java-driver-metrics-micrometer module.");
          }
        } else if (metricsCapabilityItem.metricsSupported(MetricsFactory.MP_METRICS)) {
          if (checkMicroProfileMetricsFactoryPresent()) {
            recorder.configureMicroProfileMetrics();
          } else {
            LOG.warn(
                "MicroProfile metrics were enabled by configuration, but MicroProfileMetricsFactory was not found.");
            LOG.warn(
                "Make sure to include a dependency to the java-driver-metrics-microprofile module.");
          }
        } else {
          LOG.warn(
              "Cassandra metrics were enabled by configuration, but the installed metrics capability is not supported.");
          LOG.warn(
              "Make sure to include a dependency to either quarkus-micrometer-registry-prometheus or quarkus-smallrye-metrics.");
        }
      } else {
        LOG.warn(
            "Cassandra metrics were enabled by configuration, but no metrics capability is installed.");
        LOG.warn(
            "Make sure to include a dependency to either quarkus-micrometer-registry-prometheus or quarkus-smallrye-metrics.");
      }
    } else {
      LOG.info("Cassandra metrics were disabled by configuration.");
    }
  }

  private boolean checkMicrometerMetricsFactoryPresent() {
    try {
      Class.forName("com.datastax.oss.driver.internal.metrics.micrometer.MicrometerMetricsFactory");
      return true;
    } catch (ClassNotFoundException ignored) {
      return false;
    }
  }

  private boolean checkMicroProfileMetricsFactoryPresent() {
    try {
      Class.forName(
          "com.datastax.oss.driver.internal.metrics.microprofile.MicroProfileMetricsFactory");
      return true;
    } catch (ClassNotFoundException ignored) {
      return false;
    }
  }

  @Record(STATIC_INIT)
  @BuildStep
  void configureCompression(
      CassandraClientRecorder recorder,
      CassandraClientBuildTimeConfig buildTimeConfig,
      BeanContainerBuildItem beanContainer) {
    recorder.configureCompression(buildTimeConfig.protocolCompression);
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
  @Record(RUNTIME_INIT)
  @Consume(SyntheticBeansRuntimeInitBuildItem.class)
  CassandraClientBuildItem cassandraClient(
      CassandraClientRecorder recorder,
      ShutdownContextBuildItem shutdown,
      BeanContainerBuildItem beanContainer) {
    return new CassandraClientBuildItem(recorder.buildClient(shutdown));
  }

  @BuildStep
  HealthBuildItem addHealthCheck(CassandraClientBuildTimeConfig buildTimeConfig) {
    return new HealthBuildItem(
        "com.datastax.oss.quarkus.runtime.internal.health.CassandraAsyncHealthCheck",
        buildTimeConfig.healthEnabled);
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
  RuntimeInitializedClassBuildItem runtimeNative() {
    return new RuntimeInitializedClassBuildItem(Native.class.getCanonicalName());
  }
}
