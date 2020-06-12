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
package com.datastax.oss.quarkus.runtime.internal.metrics;

import com.datastax.dse.driver.api.core.metrics.DseNodeMetric;
import com.datastax.dse.driver.api.core.metrics.DseSessionMetric;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.metrics.DefaultNodeMetric;
import com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric;
import com.datastax.oss.driver.api.core.metrics.Metrics;
import com.datastax.oss.driver.api.core.metrics.NodeMetric;
import com.datastax.oss.driver.api.core.metrics.SessionMetric;
import com.datastax.oss.driver.internal.core.context.InternalDriverContext;
import com.datastax.oss.driver.internal.core.metrics.MetricsFactory;
import com.datastax.oss.driver.internal.core.metrics.NodeMetricUpdater;
import com.datastax.oss.driver.internal.core.metrics.NoopNodeMetricUpdater;
import com.datastax.oss.driver.internal.core.metrics.NoopSessionMetricUpdater;
import com.datastax.oss.driver.internal.core.metrics.SessionMetricUpdater;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicroProfileMetricsFactory implements MetricsFactory {
  private static final Logger LOG = LoggerFactory.getLogger(MicroProfileMetricsFactory.class);

  private final String logPrefix;
  private final InternalDriverContext context;
  private final Set<NodeMetric> enabledNodeMetrics;
  private final MetricRegistry registry;
  private final SessionMetricUpdater sessionUpdater;

  public MicroProfileMetricsFactory(InternalDriverContext context, MetricRegistry metricRegistry) {
    this.logPrefix = context.getSessionName();
    this.context = context;

    DriverExecutionProfile config = context.getConfig().getDefaultProfile();
    Set<SessionMetric> enabledSessionMetrics =
        parseSessionMetricPaths(config.getStringList(DefaultDriverOption.METRICS_SESSION_ENABLED));
    this.enabledNodeMetrics =
        parseNodeMetricPaths(config.getStringList(DefaultDriverOption.METRICS_NODE_ENABLED));

    if (enabledSessionMetrics.isEmpty() && enabledNodeMetrics.isEmpty()) {
      LOG.debug("[{}] All metrics are disabled.", logPrefix);
      this.registry = null;
      this.sessionUpdater = NoopSessionMetricUpdater.INSTANCE;
    } else {
      this.registry = metricRegistry;
      this.sessionUpdater =
          new MicroProfileSessionMetricUpdater(enabledSessionMetrics, registry, context);
    }
  }

  @Override
  public Optional<Metrics> getMetrics() {
    throw new UnsupportedOperationException(
        "getMetrics() is not supported in Quarkus. The driver publishes its metrics directly to the vendor MetricRegistry of the container.");
  }

  @Override
  public SessionMetricUpdater getSessionUpdater() {
    return sessionUpdater;
  }

  @Override
  public NodeMetricUpdater newNodeUpdater(Node node) {
    return (registry == null)
        ? NoopNodeMetricUpdater.INSTANCE
        : new MicroProfileNodeMetricUpdater(node, enabledNodeMetrics, registry, context);
  }

  protected Set<SessionMetric> parseSessionMetricPaths(List<String> paths) {
    Set<SessionMetric> result = new HashSet<>();
    for (String path : paths) {
      try {
        result.add(DefaultSessionMetric.fromPath(path));
      } catch (IllegalArgumentException e) {
        try {
          result.add(DseSessionMetric.fromPath(path));
        } catch (IllegalArgumentException e1) {
          LOG.warn("[{}] Unknown session metric {}, skipping", logPrefix, path);
        }
      }
    }
    return Collections.unmodifiableSet(result);
  }

  protected Set<NodeMetric> parseNodeMetricPaths(List<String> paths) {
    Set<NodeMetric> result = new HashSet<>();
    for (String path : paths) {
      try {
        result.add(DefaultNodeMetric.fromPath(path));
      } catch (IllegalArgumentException e) {
        try {
          result.add(DseNodeMetric.fromPath(path));
        } catch (IllegalArgumentException e1) {
          LOG.warn("[{}] Unknown node metric {}, skipping", logPrefix, path);
        }
      }
    }
    return Collections.unmodifiableSet(result);
  }
}
