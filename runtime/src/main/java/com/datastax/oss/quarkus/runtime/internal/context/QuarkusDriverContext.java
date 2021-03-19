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
import com.datastax.oss.quarkus.runtime.internal.driver.QuarkusNettyOptions;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.netty.channel.EventLoopGroup;

public class QuarkusDriverContext extends DefaultDriverContext {

  private final EventLoopGroup quarkusEventLoop;

  public QuarkusDriverContext(
      @NonNull DriverConfigLoader configLoader,
      @NonNull ProgrammaticArguments programmaticArguments,
      @Nullable EventLoopGroup quarkusEventLoop) {
    super(configLoader, programmaticArguments);
    this.quarkusEventLoop = quarkusEventLoop;
  }

  @Override
  protected NettyOptions buildNettyOptions() {
    if (quarkusEventLoop != null) {
      return new QuarkusNettyOptions(this, quarkusEventLoop, quarkusEventLoop);
    } else {
      return super.buildNettyOptions();
    }
  }
}
