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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MetricsConfig {
  private boolean metricsEnabled;
  public List<String> metricsNodeEnabled;
  public List<String> metricsSessionEnabled;

  // no-args constructor needed for serialization
  public MetricsConfig() {}

  public MetricsConfig(
      Optional<List<String>> metricsNodeEnabled,
      Optional<List<String>> metricsSessionEnabled,
      boolean metricsEnabled) {

    this(
        metricsNodeEnabled.orElse(Collections.emptyList()),
        metricsSessionEnabled.orElse(Collections.emptyList()),
        metricsEnabled);
  }

  private MetricsConfig(
      List<String> metricsNodeEnabled, List<String> metricsSessionEnabled, boolean metricsEnabled) {
    this.metricsNodeEnabled = Collections.unmodifiableList(metricsNodeEnabled);
    this.metricsSessionEnabled = Collections.unmodifiableList(metricsSessionEnabled);
    this.metricsEnabled = metricsEnabled;
  }

  public List<String> getMetricsNodeEnabled() {
    return metricsNodeEnabled;
  }

  public void setMetricsNodeEnabled(List<String> metricsNodeEnabled) {
    this.metricsNodeEnabled = metricsNodeEnabled;
  }

  public List<String> getMetricsSessionEnabled() {
    return metricsSessionEnabled;
  }

  public void setMetricsSessionEnabled(List<String> metricsSessionEnabled) {
    this.metricsSessionEnabled = metricsSessionEnabled;
  }

  public boolean isMetricsEnabled() {
    return metricsEnabled;
  }

  public void setMetricsEnabled(boolean metricsEnabled) {
    this.metricsEnabled = metricsEnabled;
  }
}
