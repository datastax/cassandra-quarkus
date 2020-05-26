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
package com.datastax.oss.quarkus.runtime.driver;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.session.ProgrammaticArguments;
import com.datastax.oss.driver.api.core.session.SessionBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.netty.channel.EventLoopGroup;
import org.eclipse.microprofile.metrics.MetricRegistry;

public class QuarkusSessionBuilder extends SessionBuilder<QuarkusSessionBuilder, CqlSession> {

  private final MetricRegistry metricRegistry;
  private final EventLoopGroup mainEventLoop;
  private boolean useQuarkusNettyEventLoop;

  public QuarkusSessionBuilder(
      MetricRegistry metricRegistry,
      EventLoopGroup mainEventLoop,
      boolean useQuarkusNettyEventLoop) {

    this.metricRegistry = metricRegistry;
    this.mainEventLoop = mainEventLoop;
    this.useQuarkusNettyEventLoop = useQuarkusNettyEventLoop;
  }

  @Override
  protected CqlSession wrap(@NonNull CqlSession cqlSession) {
    return cqlSession;
  }

  @Override
  protected DriverContext buildContext(
      DriverConfigLoader configLoader, ProgrammaticArguments programmaticArguments) {
    return new QuarkusDriverContext(
        configLoader,
        programmaticArguments,
        metricRegistry,
        mainEventLoop,
        useQuarkusNettyEventLoop);
  }
}
