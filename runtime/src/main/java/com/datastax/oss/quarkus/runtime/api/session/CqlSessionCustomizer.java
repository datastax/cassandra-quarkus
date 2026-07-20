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
package com.datastax.oss.quarkus.runtime.api.session;

import com.datastax.oss.quarkus.runtime.internal.session.QuarkusCqlSessionBuilder;

/**
 * Meant to be implemented by a CDI bean that provided arbitrary customization for the default
 * CqlSession. All implementations (that are registered as CDI beans) are taken into account when
 * producing the default CqlSession.
 */
public interface CqlSessionCustomizer extends Comparable<CqlSessionCustomizer> {

  int DEFAULT_PRIORITY = 0;

  /**
   * Defines the priority that the customizers are applied. A lower integer value means that the
   * customizer will be applied after a customizer with a higher priority
   */
  default int priority() {
    return DEFAULT_PRIORITY;
  }

  void customize(QuarkusCqlSessionBuilder builder);

  default int compareTo(CqlSessionCustomizer o) {
    return Integer.compare(o.priority(), priority());
  }
}
