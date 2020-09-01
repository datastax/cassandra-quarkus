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
package com.datastax.oss.quarkus.runtime.internal.session;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.OptionsMap;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.session.ProgrammaticArguments;
import com.datastax.oss.driver.api.core.session.SessionBuilder;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.runtime.internal.context.QuarkusDriverContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.netty.channel.EventLoopGroup;
import org.eclipse.microprofile.metrics.MetricRegistry;

public class QuarkusCqlSessionBuilder
    extends SessionBuilder<QuarkusCqlSessionBuilder, QuarkusCqlSession> {

  private MetricRegistry metricRegistry;
  private EventLoopGroup quarkusEventLoop;
  private OptionsMap quarkusConfig;

  public QuarkusCqlSessionBuilder withMetricRegistry(@NonNull MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
    return this;
  }

  public QuarkusCqlSessionBuilder withQuarkusEventLoop(@Nullable EventLoopGroup quarkusEventLoop) {
    this.quarkusEventLoop = quarkusEventLoop;
    return this;
  }

  public QuarkusCqlSessionBuilder withQuarkusConfig(OptionsMap quarkusConfig) {
    this.quarkusConfig = quarkusConfig;
    return this;
  }

  @Override
  protected QuarkusCqlSession wrap(@NonNull CqlSession cqlSession) {
    return new DefaultQuarkusCqlSession(cqlSession);
  }

  @NonNull
  public QuarkusCqlSessionBuilder withConfigLoader(@Nullable DriverConfigLoader configLoader) {
    return super.withConfigLoader(overrideWithQuarkusConfig(configLoader));
  }

  @NonNull
  @Deprecated
  protected DriverConfigLoader defaultConfigLoader() {
    return overrideWithQuarkusConfig(super.defaultConfigLoader());
  }

  @NonNull
  protected DriverConfigLoader defaultConfigLoader(@Nullable ClassLoader classLoader) {
    return overrideWithQuarkusConfig(super.defaultConfigLoader(classLoader));
  }

  private DriverConfigLoader overrideWithQuarkusConfig(DriverConfigLoader configLoader) {
    return DriverConfigLoader.compose(
        DriverConfigLoader.fromMap(quarkusConfig), // takes precedence
        configLoader);
  }

  @Override
  protected DriverContext buildContext(
      DriverConfigLoader configLoader, ProgrammaticArguments programmaticArguments) {
    return new QuarkusDriverContext(
        configLoader, programmaticArguments, metricRegistry, quarkusEventLoop);
  }
}
