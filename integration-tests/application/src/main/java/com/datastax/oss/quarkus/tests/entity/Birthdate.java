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

import java.time.LocalDate;

public record Birthdate(LocalDate date) {

  public static Birthdate of(int day, int month, int year) {
    return new Birthdate(LocalDate.of(year, month, day));
  }

  public Birthdate {
    if (date.isAfter(LocalDate.now()))
      throw new IllegalArgumentException("Birthdate must be in the past");
  }
}
