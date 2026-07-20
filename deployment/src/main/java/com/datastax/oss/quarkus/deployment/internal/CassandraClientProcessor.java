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

import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.PrimitiveBooleanCodec;
import com.datastax.oss.driver.api.core.type.codec.PrimitiveByteCodec;
import com.datastax.oss.driver.api.core.type.codec.PrimitiveDoubleCodec;
import com.datastax.oss.driver.api.core.type.codec.PrimitiveFloatCodec;
import com.datastax.oss.driver.api.core.type.codec.PrimitiveIntCodec;
import com.datastax.oss.driver.api.core.type.codec.PrimitiveLongCodec;
import com.datastax.oss.driver.api.core.type.codec.PrimitiveShortCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.internal.core.metadata.MetadataManager;
import com.datastax.oss.driver.internal.core.metrics.DefaultMetricsFactory;
import com.datastax.oss.driver.internal.core.metrics.TaggingMetricIdGenerator;
import com.datastax.oss.driver.internal.core.os.Native;
import com.datastax.oss.quarkus.deployment.api.CassandraClientBuildTimeConfig;
import com.datastax.oss.quarkus.deployment.api.CassandraTypeCodecBuildItem;
import com.datastax.oss.quarkus.deployment.api.CassandraTypeCodecProviderBuildItem;
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
import io.quarkus.deployment.builditem.AdditionalIndexedClassesBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.metrics.MetricsCapabilityBuildItem;
import io.quarkus.runtime.metrics.MetricsFactory;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CassandraClientProcessor {

  public static final String CASSANDRA_CLIENT = "cassandra-client";

  private static final Logger LOG = LoggerFactory.getLogger(CassandraClientProcessor.class);

  private static final DotName TYPE_CODEC = DotName.createSimple(TypeCodec.class.getName());

  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(CASSANDRA_CLIENT);
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerGraphForReflection() {

    return Collections.singletonList(
        ReflectiveClassBuildItem.builder(
                // Required for the driver DependencyCheck mechanism
                "org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal",
                // Should be initialized at build time:
                "org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerIoRegistryV3d0",
                "org.apache.tinkerpop.shaded.jackson.databind.deser.std.StdDeserializer",
                // Required by Tinkerpop:
                // TODO check if this is really all that is instantiated by reflection
                "org.apache.tinkerpop.gremlin.structure.Graph",
                "org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph",
                "org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph",
                "org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource")
            .methods(true)
            .fields(true)
            .build());
  }

  @BuildStep
  ReflectiveClassBuildItem registerGeometryForReflection() {
    // Required for the driver DependencyCheck mechanism
    return ReflectiveClassBuildItem.builder("com.esri.core.geometry.ogc.OGCGeometry").build();
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerJsonForReflection() {
    // Required for the driver DependencyCheck mechanism
    return Collections.singletonList(
        ReflectiveClassBuildItem.builder(
                "com.fasterxml.jackson.core.JsonParser",
                "com.fasterxml.jackson.databind.ObjectMapper")
            .build());
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerReactiveForReflection() {
    // Required for the driver DependencyCheck mechanism
    return Collections.singletonList(
        ReflectiveClassBuildItem.builder("org.reactivestreams.Publisher").build());
  }

  @BuildStep
  List<ReflectiveClassBuildItem> registerLz4ForReflection(
      CassandraClientBuildTimeConfig buildTimeConfig) {
    if (buildTimeConfig.protocolCompression().equalsIgnoreCase("lz4")) {
      return Collections.singletonList(
          ReflectiveClassBuildItem.builder(
                  "net.jpountz.lz4.LZ4Compressor",
                  "net.jpountz.lz4.LZ4JavaSafeCompressor",
                  "net.jpountz.lz4.LZ4HCJavaSafeCompressor",
                  "net.jpountz.lz4.LZ4JavaSafeFastDecompressor",
                  "net.jpountz.lz4.LZ4JavaSafeSafeDecompressor",
                  "net.jpountz.lz4.LZ4JavaUnsafeCompressor",
                  "net.jpountz.lz4.LZ4HCJavaUnsafeCompressor",
                  "net.jpountz.lz4.LZ4JavaUnsafeFastDecompressor",
                  "net.jpountz.lz4.LZ4JavaUnsafeSafeDecompressor")
              .constructors(true)
              .fields(true)
              .build());
    }
    return Collections.emptyList();
  }

  @Record(STATIC_INIT)
  @BuildStep
  List<ReflectiveClassBuildItem> registerRequestTrackersForReflection(
      CassandraClientBuildTimeConfig buildTimeConfig,
      CassandraClientRecorder recorder,
      BeanContainerBuildItem beanContainer) {
    return buildTimeConfig
        .requestTrackers()
        .map(
            classes ->
                classes.stream()
                    .map(
                        clz -> {
                          recorder.addRequestTrackerClass(clz);
                          return ReflectiveClassBuildItem.builder(clz).constructors(true).build();
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
        .nodeStateListeners()
        .map(
            classes ->
                classes.stream()
                    .map(
                        clz -> {
                          recorder.addNodeStateListenerClass(clz);
                          return ReflectiveClassBuildItem.builder(clz).constructors(true).build();
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
        .schemaChangeListeners()
        .map(
            classes ->
                classes.stream()
                    .map(
                        clz -> {
                          recorder.addSchemaChangeListenerClass(clz);
                          return ReflectiveClassBuildItem.builder(clz).constructors(true).build();
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

    if (buildTimeConfig.metricsEnabled() && metricsCapability.isPresent()) {

      Stream<String> clzStream = Stream.empty();
      MetricsCapabilityBuildItem metricsCapabilityItem = metricsCapability.get();
      if (metricsCapabilityItem.metricsSupported(MetricsFactory.MICROMETER)) {
        clzStream =
            Stream.of(
                TaggingMetricIdGenerator.class.getName(),
                "com.datastax.oss.driver.internal.metrics.micrometer.MicrometerMetricsFactory");
      } else {
        LOG.warn("The cassandra-quarkus plugin only supports Micrometer");
      }
      return clzStream
          .map(
              (clzName) ->
                  ReflectiveClassBuildItem.builder(clzName).methods(true).fields(true).build())
          .collect(Collectors.toList());
    }
    return Collections.singletonList(
        ReflectiveClassBuildItem.builder(DefaultMetricsFactory.class).build());
  }

  @BuildStep
  UnremovableBeanBuildItem registerMetricsRegistry(
      Optional<MetricsCapabilityBuildItem> metricsCapability) {
    if (metricsCapability.isPresent()) {
      MetricsCapabilityBuildItem metricsCapabilityItem = metricsCapability.get();
      if (metricsCapabilityItem.metricsSupported(MetricsFactory.MICROMETER)) {
        return UnremovableBeanBuildItem.beanTypes(
            DotName.createSimple("io.micrometer.core.instrument.MeterRegistry"));
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
    if (buildTimeConfig.metricsEnabled()) {
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
        } else {
          LOG.warn(
              "Cassandra metrics were enabled by configuration, but the installed metrics capability is not supported.");
          LOG.warn("Make sure to include a dependency to quarkus-micrometer-registry-prometheus.");
        }
      } else {
        LOG.warn(
            "Cassandra metrics were enabled by configuration, but no metrics capability is installed.");
        LOG.warn("Make sure to include a dependency to quarkus-micrometer-registry-prometheus.");
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

  /**
   * Adds the codec base types provided by the driver to the Jandex index.
   *
   * <p>{@code java-driver-core} does not ship a Jandex index, so without this step the index has no
   * knowledge that e.g. {@link MappingCodec} implements {@link TypeCodec}, and application codecs
   * extending it would not be found by {@link #discoverTypeCodecs}.
   */
  @BuildStep
  AdditionalIndexedClassesBuildItem indexTypeCodecBaseTypes() {
    return new AdditionalIndexedClassesBuildItem(
        TypeCodec.class.getName(),
        MappingCodec.class.getName(),
        PrimitiveBooleanCodec.class.getName(),
        PrimitiveByteCodec.class.getName(),
        PrimitiveDoubleCodec.class.getName(),
        PrimitiveFloatCodec.class.getName(),
        PrimitiveIntCodec.class.getName(),
        PrimitiveLongCodec.class.getName(),
        PrimitiveShortCodec.class.getName());
  }

  /**
   * Finds all {@link TypeCodec} implementations in the application and turns each one into a {@link
   * CassandraTypeCodecBuildItem}.
   *
   * <p>Codecs are instantiated by {@link CassandraClientProducer} when the session is built, hence
   * only public classes with a public no-arg constructor can be registered this way.
   */
  @BuildStep
  void discoverTypeCodecs(
      CombinedIndexBuildItem combinedIndex, BuildProducer<CassandraTypeCodecBuildItem> typeCodecs) {
    for (ClassInfo codec : combinedIndex.getIndex().getAllKnownImplementations(TYPE_CODEC)) {
      String codecClassName = codec.name().toString();
      if (isDriverClass(codecClassName)) {
        // the driver registers its own codecs
        continue;
      }
      if (codec.isAbstract() || codec.isSynthetic() || !Modifier.isPublic(codec.flags())) {
        LOG.debug("Ignoring type codec {}: not a public concrete class.", codecClassName);
      } else if (!hasPublicNoArgConstructor(codec)) {
        LOG.warn(
            "Ignoring type codec {}: it has no public no-arg constructor, "
                + "which is required for automatic registration. Expose it through a public static "
                + "no-arg method returning TypeCodec<?>[] instead.",
            codecClassName);
      } else {
        LOG.debug("Found type codec: {}", codecClassName);
        typeCodecs.produce(new CassandraTypeCodecBuildItem(codecClassName));
      }
    }
  }

  /**
   * Finds all methods that hand out {@link TypeCodec} instances, that is, {@code public static}
   * no-arg methods returning {@code TypeCodec<?>[]}, and turns each one into a {@link
   * CassandraTypeCodecProviderBuildItem}.
   *
   * <p>This is how generated codecs are typically exposed: the codec classes themselves are not
   * public, but a single factory method hands out one instance of each. It is also the only way to
   * register codecs that cannot be built from a no-arg constructor.
   */
  @BuildStep
  void discoverTypeCodecProviders(
      CombinedIndexBuildItem combinedIndex,
      BuildProducer<CassandraTypeCodecProviderBuildItem> typeCodecProviders) {
    for (ClassInfo clz : combinedIndex.getIndex().getKnownClasses()) {
      if (isDriverClass(clz.name().toString())) {
        continue;
      }
      boolean declaringClassIsPublic = Modifier.isPublic(clz.flags());
      for (MethodInfo method : clz.methods()) {
        if (!isTypeCodecProviderMethod(method)) {
          continue;
        }
        if (declaringClassIsPublic) {
          LOG.debug("Found type codec provider: {}.{}()", clz.name(), method.name());
          typeCodecProviders.produce(
              new CassandraTypeCodecProviderBuildItem(clz.name().toString(), method.name()));
        } else {
          LOG.warn(
              "Ignoring type codec provider {}.{}(): its declaring class is not public.",
              clz.name(),
              method.name());
        }
      }
    }
  }

  @Record(STATIC_INIT)
  @BuildStep
  ReflectiveClassBuildItem registerTypeCodecs(
      List<CassandraTypeCodecBuildItem> typeCodecs,
      CassandraClientRecorder recorder,
      BeanContainerBuildItem beanContainer) {
    // the same codec can be contributed both by discovery and by another extension
    List<String> codecClassNames =
        typeCodecs.stream()
            .map(CassandraTypeCodecBuildItem::getCodecClassName)
            .distinct()
            .collect(Collectors.toList());
    LOG.debug("Registering type codecs: {}", codecClassNames);
    codecClassNames.forEach(recorder::addTypeCodecClass);
    // codecs are instantiated reflectively
    return ReflectiveClassBuildItem.builder(codecClassNames.toArray(new String[0]))
        .constructors(true)
        .build();
  }

  @Record(STATIC_INIT)
  @BuildStep
  ReflectiveClassBuildItem registerTypeCodecProviders(
      List<CassandraTypeCodecProviderBuildItem> typeCodecProviders,
      CassandraClientRecorder recorder,
      BeanContainerBuildItem beanContainer) {
    // the same provider can be contributed both by discovery and by another extension
    List<CassandraTypeCodecProviderBuildItem> providers =
        typeCodecProviders.stream().distinct().toList();
    for (CassandraTypeCodecProviderBuildItem provider : providers) {
      LOG.debug("Registering type codec provider: {}", provider);
      recorder.addTypeCodecProvider(provider.getClassName(), provider.getMethodName());
    }
    // provider methods are invoked reflectively
    return ReflectiveClassBuildItem.builder(
            providers.stream()
                .map(CassandraTypeCodecProviderBuildItem::getClassName)
                .distinct()
                .toArray(String[]::new))
        .methods(true)
        .build();
  }

  private static boolean isDriverClass(String className) {
    return className.startsWith("com.datastax.oss.driver.")
        || className.startsWith("com.datastax.dse.driver.");
  }

  private static boolean hasPublicNoArgConstructor(ClassInfo clz) {
    return clz.constructors().stream()
        .anyMatch(
            constructor ->
                constructor.parametersCount() == 0 && Modifier.isPublic(constructor.flags()));
  }

  private static boolean isTypeCodecProviderMethod(MethodInfo method) {
    return Modifier.isPublic(method.flags())
        && Modifier.isStatic(method.flags())
        && method.parametersCount() == 0
        && isTypeCodecArray(method.returnType());
  }

  private static boolean isTypeCodecArray(Type type) {
    return type.kind() == Type.Kind.ARRAY
        && type.asArrayType().dimensions() == 1
        && type.asArrayType().constituent().name().equals(TYPE_CODEC);
  }

  @Record(STATIC_INIT)
  @BuildStep
  void configureCompression(
      CassandraClientRecorder recorder,
      CassandraClientBuildTimeConfig buildTimeConfig,
      BeanContainerBuildItem beanContainer) {
    recorder.configureCompression(buildTimeConfig.protocolCompression());
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
        buildTimeConfig.healthEnabled());
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
