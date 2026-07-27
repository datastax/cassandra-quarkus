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

public record Born(int value) {

  public Born {
    var currentYear = LocalDate.now().getYear();
    if (value < 1900 || value > currentYear)
      throw new IllegalArgumentException("Born must be between 1900 and " + currentYear);
  }

  public static Born inYear(int year) {
    return new Born(year);
  }

  public static Born ofAge(int age) {
    return new Born(LocalDate.now().getYear() - age);
  }
}
