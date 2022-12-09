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
package com.datastax.oss.quarkus.tests;

import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;

@QuarkusTestResource(
    value = CassandraTestResource.class,
    initArgs = {
      @ResourceArg(
          name = "quarkus.cassandra.test.container.image",
          value = "datastax/dse-server:6.8.25"),
      // activate DSE Graph with the -g switch; the resulting command will be: dse cassandra -f -g
      @ResourceArg(name = "quarkus.cassandra.test.container.cmd", value = "-g"),
      @ResourceArg(name = "quarkus.cassandra.test.container.startup-timeout", value = "PT5M")
    })
public abstract class DseTestBase {}
