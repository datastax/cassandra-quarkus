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
package com.datastax.oss.quarkus.demo;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.PropertyStrategy;
import lombok.Data;

/**
 * Represents the name and description of a fruit.
 *
 * @see <a
 *     href="https://docs.datastax.com/en/developer/java-driver/latest/manual/mapper/entities/">Defining
 *     entities with the DataStax Java driver object mapper</a>
 */
@Data
@Entity
@PropertyStrategy(mutable = false)
public class Fruit {

  @PartitionKey private final String name;

  private final String description;
  private final Type type;

  public enum Type {
    LOCAL,
    DELIVERED
  }
}
