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
package com.datastax.oss.quarkus.runtime.internal.reactive;

import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.dse.driver.internal.core.cql.reactive.DefaultReactiveResultSet;
import io.smallrye.mutiny.Multi;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MutinyReactiveResultSetTest {

  @Test
  public void should_validate_reactive_result_set() {
    // given
    List<Integer> items = new ArrayList<>();

    // when

    Multi<Integer> resultSet =
        new DefaultMutinyReactiveResultSet(
                new DefaultReactiveResultSet(() -> MockAsyncResultSet.createResults(4, 5)))
            .map(row -> row.getInt(0));

    // then
    resultSet.subscribe().with(items::add);
    assertThat(items.size()).isEqualTo(20);
  }
}
