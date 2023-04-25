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

package com.datastax.oss.quarkus.deployment.api;

import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.LaunchMode;
import java.io.Closeable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.ConfigValue;
import org.jboss.logging.Logger;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

@BuildSteps(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
public class CassandraDevServicesProcessor {
  private static final Logger log = Logger.getLogger(CassandraDevServicesProcessor.class);

  private static final String DEV_SERVICE_LABEL = "quarkus-dev-service-cassandra";

  private static final int CASSANDRA_PORT = 9042;

  private static final ContainerLocator cassandraContainerLocator =
      new ContainerLocator(DEV_SERVICE_LABEL, CASSANDRA_PORT);
  static volatile RunningDevService devService;
  static volatile CassandraDevServiceCfg cfg;
  static volatile boolean first = true;

  @BuildStep
  public DevServicesResultBuildItem startCassandraDevService(
      DockerStatusBuildItem dockerStatusBuildItem,
      LaunchModeBuildItem launchMode,
      CassandraClientBuildTimeConfig cassandraClientBuildTimeConfig,
      Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
      LoggingSetupBuildItem loggingSetupBuildItem,
      GlobalDevServicesConfig devServicesConfig) {

    CassandraDevServiceCfg configuration = getConfiguration(cassandraClientBuildTimeConfig);

    if (devService != null) {
      boolean shouldShutdownTheCassandra = !configuration.equals(cfg);
      if (!shouldShutdownTheCassandra) {
        return devService.toBuildItem();
      }
      shutdownCassandra();
      cfg = null;
    }

    StartupLogCompressor compressor =
        new StartupLogCompressor(
            (launchMode.isTest() ? "(test) " : "") + "Cassandra Dev Services Starting:",
            consoleInstalledBuildItem,
            loggingSetupBuildItem);
    try {
      DevServicesResultBuildItem.RunningDevService newDevService =
          startCassandra(
              dockerStatusBuildItem, configuration, launchMode, devServicesConfig.timeout);
      if (newDevService != null) {
        devService = newDevService;

        Map<String, String> config = devService.getConfig();
        if (devService.isOwner()) {
          log.info("Dev Services for Cassandra started.");
        }
      }
      if (devService == null) {
        compressor.closeAndDumpCaptured();
      } else {
        compressor.close();
      }
    } catch (Throwable t) {
      compressor.closeAndDumpCaptured();
      throw new RuntimeException(t);
    }

    if (devService == null) {
      return null;
    }

    // Configure the watch dog
    if (first) {
      first = false;
      Runnable closeTask =
          () -> {
            if (devService != null) {
              shutdownCassandra();

              log.info("Dev Services for Cassandra shut down.");
            }
            first = true;
            devService = null;
            cfg = null;
          };
      QuarkusClassLoader cl = (QuarkusClassLoader) Thread.currentThread().getContextClassLoader();
      ((QuarkusClassLoader) cl.parent()).addCloseTask(closeTask);
    }
    cfg = configuration;
    return devService.toBuildItem();
  }

  private void shutdownCassandra() {
    if (devService != null) {
      try {
        devService.close();
      } catch (Throwable e) {
        log.error("Failed to stop the Cassandra", e);
      } finally {
        devService = null;
      }
    }
  }

  private DevServicesResultBuildItem.RunningDevService startCassandra(
      DockerStatusBuildItem dockerStatusBuildItem,
      CassandraDevServiceCfg config,
      LaunchModeBuildItem launchMode,
      Optional<Duration> timeout) {
    if (!config.devServicesEnabled) {
      // explicitly disabled
      log.debug("Not starting Dev Services for Cassandra, as it has been disabled in the config.");
      return null;
    }

    // Verify that we have Cassandra without contact points
    if (!hasCassandraContactPointsWithoutHostAndPort()) {
      log.debug("Not starting Dev Services for Cassandra, all the channels are configured.");
      return null;
    }

    if (!dockerStatusBuildItem.isDockerAvailable()) {
      log.warn("Docker isn't working, please configure the Cassandra location.");
      return null;
    }

    ConfiguredCassandraContainer container =
        new ConfiguredCassandraContainer(
            DockerImageName.parse(config.imageName).asCompatibleSubstituteFor("cassandra"),
            config.fixedExposedPort,
            launchMode.getLaunchMode() == LaunchMode.DEVELOPMENT ? config.serviceName : null,
            config.initScript);

    final Supplier<DevServicesResultBuildItem.RunningDevService> defaultCassandraSupplier =
        () -> {

          // Starting cassandra
          timeout.ifPresent(container::withStartupTimeout);
          container.start();
          return getRunningDevService(
              container.getContainerId(),
              container::close,
              container.getHost(),
              container.getPort());
        };

    return cassandraContainerLocator
        .locateContainer(config.serviceName, config.shared, launchMode.getLaunchMode())
        .map(
            containerAddress ->
                getRunningDevService(
                    containerAddress.getId(),
                    null,
                    containerAddress.getHost(),
                    containerAddress.getPort()))
        .orElseGet(defaultCassandraSupplier);
  }

  private boolean hasCassandraContactPointsWithoutHostAndPort() {
    Config config = ConfigProvider.getConfig();
    // TODO: support for named connections?
    // TODO: refactor validation of contact points
    // for (String name : config.getPropertyNames()) {
    //   if (name.equals("quarkus.cassandra.contact-points")
    //       && config.getValue(name, String.class).isBlank()) {
    //     return true;
    //   }
    // }
    ConfigValue configValue = config.getConfigValue("quarkus.cassandra.contact-points");
    return configValue.getValue() == null || configValue.getValue().isBlank();
  }

  private DevServicesResultBuildItem.RunningDevService getRunningDevService(
      String containerId, Closeable closeable, String host, int port) {
    Map<String, String> configMap = new HashMap<>();
    configMap.putIfAbsent("quarkus.cassandra.contact-points", String.format("%s:%d", host, port));
    configMap.putIfAbsent("quarkus.cassandra.local-datacenter", "datacenter1");
    return new DevServicesResultBuildItem.RunningDevService(
        "CASSANDRA", containerId, closeable, configMap);
  }

  private CassandraDevServiceCfg getConfiguration(CassandraClientBuildTimeConfig cfg) {
    CassandraDevServicesBuildTimeConfig devServicesConfig = cfg.devservices;
    return new CassandraDevServiceCfg(devServicesConfig);
  }

  private static final class CassandraDevServiceCfg {

    private final boolean devServicesEnabled;
    private final String imageName;
    private final Integer fixedExposedPort;
    private final boolean shared;
    private final String serviceName;

    private final String initScript;

    public CassandraDevServiceCfg(CassandraDevServicesBuildTimeConfig devServicesConfig) {
      this.devServicesEnabled = devServicesConfig.enabled.orElse(true);
      this.imageName = devServicesConfig.imageName;
      this.fixedExposedPort = devServicesConfig.port.orElse(0);
      this.shared = devServicesConfig.shared;
      this.serviceName = devServicesConfig.serviceName;
      this.initScript = devServicesConfig.initScript.orElse(null);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CassandraDevServiceCfg that = (CassandraDevServiceCfg) o;
      return devServicesEnabled == that.devServicesEnabled
          && Objects.equals(imageName, that.imageName)
          && Objects.equals(fixedExposedPort, that.fixedExposedPort);
    }

    @Override
    public int hashCode() {
      return Objects.hash(devServicesEnabled, imageName, fixedExposedPort);
    }
  }

  private static final class ConfiguredCassandraContainer
      extends CassandraContainer<ConfiguredCassandraContainer> {

    private final int port;

    private ConfiguredCassandraContainer(
        DockerImageName dockerImageName,
        int fixedExposedPort,
        String serviceName,
        String initScript) {
      super(dockerImageName);
      this.port = fixedExposedPort;
      withExposedPorts(CASSANDRA_PORT);
      if (initScript != null) {
        withInitScript(initScript);
      }
      withNetwork(Network.SHARED);
      if (serviceName != null) { // Only adds the label in dev mode.
        withLabel(DEV_SERVICE_LABEL, serviceName);
      }
      if (!dockerImageName.getRepository().endsWith("cassandra")) {
        throw new IllegalArgumentException("Only official cassandra images are supported");
      }
    }

    @Override
    protected void configure() {
      super.configure();
      if (port > 0) {
        addFixedExposedPort(port, CASSANDRA_PORT);
      }
    }

    public int getPort() {
      return getMappedPort(CASSANDRA_PORT);
    }
  }
}
