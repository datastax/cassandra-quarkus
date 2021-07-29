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
package com.datastax.oss.quarkus.tests.driver;

import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.internal.core.loadbalancing.DefaultLoadBalancingPolicy;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A dummy {@link com.datastax.oss.driver.api.core.loadbalancing.LoadBalancingPolicy
 * LoadBalancingPolicy} to test that custom user-supplied classes loaded using reflection by the
 * driver can be used normally.
 */
@RegisterForReflection
public class CustomLoadBalancingPolicy extends DefaultLoadBalancingPolicy {

  public CustomLoadBalancingPolicy(@NonNull DriverContext context, @NonNull String profileName) {
    super(context, profileName);
  }

  @NonNull
  @Override
  protected Optional<String> discoverLocalDc(@NonNull Map<UUID, Node> nodes) {
    return Optional.empty();
  }
}
