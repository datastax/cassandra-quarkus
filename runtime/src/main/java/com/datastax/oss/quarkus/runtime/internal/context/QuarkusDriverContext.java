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
package com.datastax.oss.quarkus.runtime.internal.context;

import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.session.ProgrammaticArguments;
import com.datastax.oss.driver.internal.core.context.DefaultDriverContext;
import com.datastax.oss.driver.internal.core.context.NettyOptions;
import com.datastax.oss.driver.internal.core.metrics.MetricsFactory;
import com.datastax.oss.quarkus.runtime.driver.QuarkusNettyOptions;
import com.datastax.oss.quarkus.runtime.metrics.MicroProfileMetricsFactory;
import io.netty.channel.EventLoopGroup;
import org.eclipse.microprofile.metrics.MetricRegistry;

public class QuarkusDriverContext extends DefaultDriverContext {

  private final MetricRegistry metricRegistry;
  private final EventLoopGroup mainEventLoop;
  private final boolean useQuarkusNettyEventLoop;

  public QuarkusDriverContext(
      DriverConfigLoader configLoader,
      ProgrammaticArguments programmaticArguments,
      MetricRegistry metricRegistry,
      EventLoopGroup mainEventLoop,
      boolean useQuarkusNettyEventLoop) {
    super(configLoader, programmaticArguments);
    this.metricRegistry = metricRegistry;
    this.mainEventLoop = mainEventLoop;
    this.useQuarkusNettyEventLoop = useQuarkusNettyEventLoop;
  }

  @Override
  protected MetricsFactory buildMetricsFactory() {
    return new MicroProfileMetricsFactory(this, metricRegistry);
  }

  @Override
  protected NettyOptions buildNettyOptions() {
    if (useQuarkusNettyEventLoop) {
      return new QuarkusNettyOptions(this, mainEventLoop, mainEventLoop);
    } else {
      return super.buildNettyOptions();
    }
  }
}
