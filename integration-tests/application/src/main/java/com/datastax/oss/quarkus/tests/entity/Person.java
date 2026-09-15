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
package com.datastax.oss.quarkus.tests.entity;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import java.util.Objects;
import java.util.UUID;

/**
 * An entity whose only purpose is to exercise user-supplied type codecs: {@link #born} has no
 * built-in codec, so persisting this entity only works if {@link
 * com.datastax.oss.quarkus.tests.driver.BornCodec} was discovered and registered on the session.
 *
 * <p>Kept separate from {@link Customer} on purpose, so that the basic CRUD tests stay a canary for
 * the common case and do not depend on codec discovery.
 */
@Entity
public class Person {

  @PartitionKey private UUID id;
  private String name;
  private Born born;

  public Person() {}

  public Person(UUID id, String name, Born born) {
    this.id = id;
    this.name = name;
    this.born = born;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Born getBorn() {
    return born;
  }

  public void setBorn(Born born) {
    this.born = born;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Person)) {
      return false;
    }
    Person that = (Person) o;
    return Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(born, that.born);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, born);
  }

  @Override
  public String toString() {
    return "Person{id=" + id + ", name='" + name + '\'' + ", born=" + born + '}';
  }
}
