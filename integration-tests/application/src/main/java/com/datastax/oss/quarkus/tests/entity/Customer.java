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
import com.datastax.oss.driver.api.mapper.annotations.NamingStrategy;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.quarkus.tests.mapper.CustomerNameConverter;
import java.util.Objects;
import java.util.UUID;

@Entity
@NamingStrategy(customConverterClass = CustomerNameConverter.class)
public class Customer {

  @PartitionKey private UUID id;

  private String name;

  private Address address;

  public Customer() {}

  public Customer(UUID id, String name, Address address) {
    this.id = id;
    this.name = name;
    this.address = address;
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

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Customer)) {
      return false;
    }
    Customer customer = (Customer) o;
    return id.equals(customer.id)
        && name.equals(customer.name)
        && Objects.equals(address, customer.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, address);
  }

  @Override
  public String toString() {
    return "Customer{id=" + id + ", name='" + name + "'}";
  }
}
