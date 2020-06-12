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

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import java.util.Objects;

@Entity
public class Fruit {

  @PartitionKey private String storeId;
  @ClusteringColumn private String name;
  private String description;

  public Fruit() {}

  public Fruit(String storeId, String name, String description) {
    this.name = name;
    this.description = description;
    this.storeId = storeId;
  }

  public String getStoreId() {
    return storeId;
  }

  public void setStoreId(String storeId) {
    this.storeId = storeId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Fruit that = (Fruit) o;
    return Objects.equals(storeId, that.storeId)
        && Objects.equals(description, that.description)
        && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(storeId, description, name);
  }

  @Override
  public String toString() {
    return "Fruit{"
        + "name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", storeId='"
        + storeId
        + '\''
        + '}';
  }
}
