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
package com.datastax.oss.quarkus.dao.nameconverters;

import com.datastax.oss.driver.api.mapper.entity.naming.NameConverter;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class TestNameConverter implements NameConverter {

    @Override
    @NonNull
    public String toCassandraName(@NonNull String javaName) {
        return "test_" + javaName;
    }
}
