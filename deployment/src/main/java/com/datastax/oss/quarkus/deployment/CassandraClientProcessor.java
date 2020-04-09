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
package com.datastax.oss.quarkus.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import com.datastax.dse.driver.internal.core.tracker.MultiplexingRequestTracker;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.internal.core.addresstranslation.Ec2MultiRegionAddressTranslator;
import com.datastax.oss.driver.internal.core.addresstranslation.PassThroughAddressTranslator;
import com.datastax.oss.driver.internal.core.connection.ConstantReconnectionPolicy;
import com.datastax.oss.driver.internal.core.connection.ExponentialReconnectionPolicy;
import com.datastax.oss.driver.internal.core.loadbalancing.DcInferringLoadBalancingPolicy;
import com.datastax.oss.driver.internal.core.loadbalancing.DefaultLoadBalancingPolicy;
import com.datastax.oss.driver.internal.core.metadata.MetadataManager;
import com.datastax.oss.driver.internal.core.metadata.NoopNodeStateListener;
import com.datastax.oss.driver.internal.core.metadata.schema.NoopSchemaChangeListener;
import com.datastax.oss.driver.internal.core.retry.DefaultRetryPolicy;
import com.datastax.oss.driver.internal.core.session.throttling.ConcurrencyLimitingRequestThrottler;
import com.datastax.oss.driver.internal.core.session.throttling.PassThroughRequestThrottler;
import com.datastax.oss.driver.internal.core.session.throttling.RateLimitingRequestThrottler;
import com.datastax.oss.driver.internal.core.specex.ConstantSpeculativeExecutionPolicy;
import com.datastax.oss.driver.internal.core.specex.NoSpeculativeExecutionPolicy;
import com.datastax.oss.driver.internal.core.time.AtomicTimestampGenerator;
import com.datastax.oss.driver.internal.core.time.ThreadLocalTimestampGenerator;
import com.datastax.oss.driver.internal.core.tracker.NoopRequestTracker;
import com.datastax.oss.driver.internal.core.tracker.RequestLogger;
import com.datastax.oss.quarkus.config.CassandraClientConfig;
import com.datastax.oss.quarkus.runtime.AbstractCassandraClientProducer;
import com.datastax.oss.quarkus.runtime.CassandraClientRecorder;
import com.datastax.oss.quarkus.runtime.metrics.MetricsConfig;
import io.quarkus.arc.Unremovable;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.recording.RecorderContext;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import org.eclipse.microprofile.metrics.MetricRegistry;

class CassandraClientProcessor {
  public static final String CASSANDRA_CLIENT = "cassandra-client";

  @BuildStep
  List<ReflectiveClassBuildItem> registerForReflection() {
    return Arrays.asList(
        // reconnection policies
        new ReflectiveClassBuildItem(true, true, ExponentialReconnectionPolicy.class.getName()),
        new ReflectiveClassBuildItem(true, true, ConstantReconnectionPolicy.class.getName()),
        // schema change listener
        new ReflectiveClassBuildItem(true, true, NoopSchemaChangeListener.class.getName()),
        // address translators
        new ReflectiveClassBuildItem(true, true, PassThroughAddressTranslator.class.getName()),
        new ReflectiveClassBuildItem(true, true, Ec2MultiRegionAddressTranslator.class.getName()),
        // load balancing policies
        new ReflectiveClassBuildItem(true, true, DefaultLoadBalancingPolicy.class.getName()),
        new ReflectiveClassBuildItem(true, true, DcInferringLoadBalancingPolicy.class.getName()),
        // retry policy
        new ReflectiveClassBuildItem(true, true, DefaultRetryPolicy.class.getName()),
        // speculative execution policies
        new ReflectiveClassBuildItem(true, true, NoSpeculativeExecutionPolicy.class.getName()),
        new ReflectiveClassBuildItem(
            true, true, ConstantSpeculativeExecutionPolicy.class.getName()),
        // state listener
        new ReflectiveClassBuildItem(true, true, NoopNodeStateListener.class.getName()),
        // request trackers
        new ReflectiveClassBuildItem(true, true, NoopRequestTracker.class.getName()),
        new ReflectiveClassBuildItem(true, true, MultiplexingRequestTracker.class.getName()),
        new ReflectiveClassBuildItem(true, true, RequestLogger.class.getName()),
        // request throttlers
        new ReflectiveClassBuildItem(true, true, PassThroughRequestThrottler.class.getName()),
        new ReflectiveClassBuildItem(
            true, true, ConcurrencyLimitingRequestThrottler.class.getName()),
        new ReflectiveClassBuildItem(true, true, RateLimitingRequestThrottler.class.getName()),
        // timestamp generators
        new ReflectiveClassBuildItem(true, true, AtomicTimestampGenerator.class.getName()),
        new ReflectiveClassBuildItem(true, true, ThreadLocalTimestampGenerator.class.getName()));
  }

  @SuppressWarnings("unchecked")
  @Record(STATIC_INIT)
  @BuildStep
  BeanContainerListenerBuildItem build(
      RecorderContext recorderContext,
      CassandraClientRecorder recorder,
      BuildProducer<FeatureBuildItem> feature,
      BuildProducer<GeneratedBeanBuildItem> generatedBean) {

    feature.produce(new FeatureBuildItem(CASSANDRA_CLIENT));

    String cassandraClientProducerClassName = getCassandraClientProducerClassName();
    createCassandraClientProducerBean(generatedBean, cassandraClientProducerClassName);

    return new BeanContainerListenerBuildItem(
        recorder.addCassandraClient(
            (Class<? extends AbstractCassandraClientProducer>)
                recorderContext.classProxy(cassandraClientProducerClassName)));
  }

  private String getCassandraClientProducerClassName() {
    return AbstractCassandraClientProducer.class.getPackage().getName()
        + "."
        + "CassandraClientProducer";
  }

  private void createCassandraClientProducerBean(
      BuildProducer<GeneratedBeanBuildItem> generatedBean,
      String cassandraClientProducerClassName) {

    ClassOutput classOutput = new GeneratedBeanGizmoAdaptor(generatedBean);

    try (ClassCreator classCreator =
        ClassCreator.builder()
            .classOutput(classOutput)
            .className(cassandraClientProducerClassName)
            .superClass(AbstractCassandraClientProducer.class)
            .build()) {
      classCreator.addAnnotation(ApplicationScoped.class);

      try (MethodCreator defaultCassandraClient =
          classCreator.getMethodCreator("createDefaultCassandraClient", CqlSession.class)) {
        defaultCassandraClient.addAnnotation(ApplicationScoped.class);
        defaultCassandraClient.addAnnotation(Produces.class);
        defaultCassandraClient.addAnnotation(Default.class);

        // make CqlSession as Unremovable bean
        defaultCassandraClient.addAnnotation(Unremovable.class);

        ResultHandle cassandraClientConfig =
            defaultCassandraClient.invokeVirtualMethod(
                MethodDescriptor.ofMethod(
                    AbstractCassandraClientProducer.class,
                    "getCassandraClientConfig",
                    CassandraClientConfig.class),
                defaultCassandraClient.getThis());

        ResultHandle metricsConfig =
            defaultCassandraClient.invokeVirtualMethod(
                MethodDescriptor.ofMethod(
                    AbstractCassandraClientProducer.class, "getMetricsConfig", MetricsConfig.class),
                defaultCassandraClient.getThis());

        ResultHandle metricRegistry =
            defaultCassandraClient.invokeVirtualMethod(
                MethodDescriptor.ofMethod(
                    AbstractCassandraClientProducer.class,
                    "getMetricRegistry",
                    MetricRegistry.class),
                defaultCassandraClient.getThis());

        defaultCassandraClient.returnValue(
            defaultCassandraClient.invokeVirtualMethod(
                MethodDescriptor.ofMethod(
                    AbstractCassandraClientProducer.class,
                    "createCassandraClient",
                    CqlSession.class,
                    CassandraClientConfig.class,
                    MetricsConfig.class,
                    MetricRegistry.class),
                defaultCassandraClient.getThis(),
                cassandraClientConfig,
                metricsConfig,
                metricRegistry));
      }
    }
  }

  @Record(RUNTIME_INIT)
  @BuildStep
  void configureRuntimeProperties(
      CassandraClientRecorder recorder,
      CassandraClientConfig runtimeConfig,
      CassandraClientBuildTimeConfig buildTimeConfig,
      Capabilities capabilities) {
    recorder.configureRuntimeProperties(runtimeConfig);

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
  @Record(value = RUNTIME_INIT, optional = true)
  CassandraClientBuildItem cassandraClient(CassandraClientRecorder recorder) {
    return new CassandraClientBuildItem(recorder.getClient());
  }

  @BuildStep
  HealthBuildItem addHealthCheck(CassandraClientBuildTimeConfig buildTimeConfig) {
    return new HealthBuildItem(
        "com.datastax.oss.quarkus.runtime.health.CassandraHealthCheck",
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
}
