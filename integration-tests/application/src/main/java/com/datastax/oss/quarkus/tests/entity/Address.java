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
import java.util.Objects;

@Entity
public class Address {

  private String street;
  private String zip;
  private String city;

  public Address() {}

  public Address(String street, String zip, String city) {
    this.street = street;
    this.zip = zip;
    this.city = city;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getZip() {
    return zip;
  }

  public void setZip(String zip) {
    this.zip = zip;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Address address = (Address) o;
    return street.equals(address.street) && zip.equals(address.zip) && city.equals(address.city);
  }

  @Override
  public int hashCode() {
    return Objects.hash(street, zip, city);
  }

  @Override
  public String toString() {
    return "Address{"
        + "street='"
        + street
        + '\''
        + ", zip='"
        + zip
        + '\''
        + ", city='"
        + city
        + '\''
        + '}';
  }
}
