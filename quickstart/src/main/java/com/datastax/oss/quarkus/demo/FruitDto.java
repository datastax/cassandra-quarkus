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

import java.util.Objects;

/** A DTO (Data Transfer Object) used to convey information from a {@link Fruit} domain object. */
public class FruitDto {

  private String name;
  private String description;

  public FruitDto() {}

  public FruitDto(String name, String description) {
    this.name = name;
    this.description = description;
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

    FruitDto fruitDto = (FruitDto) o;

    if (!Objects.equals(name, fruitDto.name)) return false;
    return Objects.equals(description, fruitDto.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, name);
  }
}
