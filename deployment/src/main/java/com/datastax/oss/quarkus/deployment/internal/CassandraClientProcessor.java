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

import static com.datastax.oss.quarkus.deployment.internal.CodecRegistrationSupport.*;
import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jboss.jandex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CassandraClientProcessor {

  public static final String CASSANDRA_CLIENT = "cassandra-client";

  private static final Logger LOG = LoggerFactory.getLogger(CassandraClientProcessor.class);

  /**
   * The driver types an application codec is written against: {@link TypeCodec} to implement from
   * scratch, {@link MappingCodec} to map a custom type onto a type the driver already handles.
   *
   * <p>Application codecs are found by asking the index for the subtypes of these. That works even
   * though {@code java-driver-core} ships no Jandex index: Jandex records subtypes keyed by
   * supertype name whether the supertype itself was indexed or not, and resolves them transitively,
   * so {@code MyCodec extends MyBase extends MappingCodec} is found as well.
   *
   * <p>The driver's other codec base types are deliberately not listed, because nothing outside the
   * driver implements them. The {@code PrimitiveXxxCodec} interfaces only exist so that the
   * built-in codecs can offer unboxed access, and a codec competing with a built-in one is refused
   * by the driver's own registry anyway. The {@code Abstract*ToArray} and geometry families take
   * their element codec or dimension as a constructor argument, so they cannot be instantiated from
   * a no-arg constructor and have to be handed out by a provider method regardless.
   *
   * <p>A codec whose parent chain leaves the index - because a base class of it lives in a
   * dependency without a Jandex index - cannot be found here either, and also needs a provider
   * method, which {@link #discoverCodecProviderMethods} finds by signature.
   */
  private static final List<DotName> CODEC_BASE_TYPES =
      Stream.of(TypeCodec.class, MappingCodec.class)
          .map(clz -> DotName.createSimple(clz.getName()))
          .toList();

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
   * Finds all {@link TypeCodec} implementations in the application and turns each one into a {@link
   * CassandraTypeCodecBuildItem}.
   *
   * <p>Codecs are instantiated by {@link CassandraClientProducer} when the session is built, hence
   * only public classes with a public no-arg constructor can be registered this way; the rest have
   * to be handed out by a provider method, see {@link #discoverCodecProviderMethods}.
   */
  @BuildStep
  void discoverCodecs(
      CombinedIndexBuildItem combinedIndex,
      BuildProducer<CassandraTypeCodecBuildItem> buildProducerCodecs,
      CassandraClientBuildTimeConfig buildTimeConfig) {
    IndexView index = combinedIndex.getIndex();
    Predicate<String> excluded =
        codecExclusions(buildTimeConfig.excludedCodecs().orElse(Collections.emptyList()));
    Predicate<String> included =
        codecInclusions(buildTimeConfig.includedCodecs().orElse(Collections.emptyList()));
    Set<ClassInfo> candidates = new LinkedHashSet<>();

    // only scan index when enabled
    if (buildTimeConfig.codecDiscoveryEnabled()) {
      for (DotName baseType : CODEC_BASE_TYPES) {
        candidates.addAll(index.getAllKnownSubclasses(baseType));
        candidates.addAll(index.getAllKnownImplementations(baseType));
      }
    }
    buildTimeConfig.includedCodecs().orElse(Collections.emptyList()).stream()
        .map(String::trim)
        .filter(fqn -> !fqn.isEmpty())
        .forEach(
            fqn -> {
              ClassInfo codecClass = index.getClassByName(DotName.createSimple(fqn));
              if (codecClass == null) {
                // The class is not in the Jandex index at all (e.g. it lives in a dependency
                // that ships no index): it cannot be validated here, so register it directly
                // by name and let instantiation at runtime surface any problem.
                LOG.info(
                    "Codec {} from quarkus.cassandra.codecs.include is not in the Jandex index; "
                        + "registering it by name without validation, so it could fail at runtime.",
                    fqn);
                buildProducerCodecs.produce(new CassandraTypeCodecBuildItem(fqn));
              } else if (candidates.contains(codecClass)) {
                LOG.warn(
                    "Ignoring codec {} configured via config-include: it was already found by discovery.",
                    codecClass.name());
              } else {
                candidates.add(codecClass);
              }
            });

    for (ClassInfo codec : candidates) {
      String codecClassName = codec.name().toString();
      if (isDriverClass(codecClassName)) {
        // the driver registers its own codecs
        continue;
      }
      if (excluded.test(codecClassName) && !included.test(codecClassName)) {
        LOG.debug("Ignoring type codec {}: excluded by configuration.", codecClassName);
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
        buildProducerCodecs.produce(new CassandraTypeCodecBuildItem(codecClassName));
      }
    }
  }

  /** Finds all methods that are annotated with @CassandraCodecProvider */
  @BuildStep
  void discoverCodecProviderMethods(
      CombinedIndexBuildItem combinedIndex,
      BuildProducer<CassandraTypeCodecProviderBuildItem> typeCodecProviders) {

    for (AnnotationInstance annotation :
        combinedIndex.getIndex().getAnnotations(CODEC_PROVIDER_ANNOTATION)) {
      AnnotationTarget target = annotation.target();
      if (target.kind() == AnnotationTarget.Kind.METHOD) {
        MethodInfo method = target.asMethod();
        ClassInfo clz = method.declaringClass();

        if (!isCodecProviderMethod(method)) {
          LOG.warn(
              "CodecProvider method not valid: {}.{}(). Must be public static method returning TypeCodec<?>[]",
              clz.name(),
              method.name());
          // todo better report an error?
        }
        boolean declaringClassIsPublic = Modifier.isPublic(clz.flags());
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
  ReflectiveClassBuildItem registerCodecs(
      List<CassandraTypeCodecBuildItem> discoveredCodecs,
      CassandraClientRecorder recorder,
      // must not be removed even when unused
      BeanContainerBuildItem beanContainer) {
    // The same codec can be contributed both by discovery and by another extension. Sorting keeps
    // the registration order stable from build to build: the index hands out classes in no
    // particular order, and the driver keeps whichever codec was registered first for a given type.
    List<String> codecClassNames =
        discoveredCodecs.stream()
            .map(CassandraTypeCodecBuildItem::getCodecClassName)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    LOG.info("Registering Cassandra codecs: {}", codecClassNames);
    codecClassNames.forEach(recorder::addTypeCodecClass);
    // codecs are instantiated reflectively
    return ReflectiveClassBuildItem.builder(codecClassNames.toArray(new String[0]))
        .constructors(true)
        .build();
  }

  @Record(STATIC_INIT)
  @BuildStep
  ReflectiveClassBuildItem registerCodecProviders(
      List<CassandraTypeCodecProviderBuildItem> codecProviders,
      CassandraClientRecorder recorder,
      // must not be removed even when unused
      BeanContainerBuildItem beanContainer) {
    // the same provider can be contributed both by discovery and by another extension; sorting
    // keeps the registration order stable from build to build
    List<CassandraTypeCodecProviderBuildItem> providers =
        codecProviders.stream()
            .distinct()
            .sorted(
                Comparator.comparing(CassandraTypeCodecProviderBuildItem::getClassName)
                    .thenComparing(CassandraTypeCodecProviderBuildItem::getMethodName))
            .toList();
    for (CassandraTypeCodecProviderBuildItem provider : providers) {
      LOG.debug("Registering Cassandra codec provider method: {}", provider);
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
