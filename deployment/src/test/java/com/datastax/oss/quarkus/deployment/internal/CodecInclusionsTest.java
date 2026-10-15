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
package com.datastax.oss.quarkus.deployment.internal;

import static com.datastax.oss.quarkus.deployment.internal.CodecRegistrationSupport.codecInclusions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.quarkus.runtime.configuration.ConfigurationException;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests the {@code quarkus.cassandra.codecs.include} pattern syntax. */
@DisplayName("Codec inclusion patterns")
public class CodecInclusionsTest {

  @Test
  public void should_include_nothing_when_no_pattern_configured() {
    Predicate<String> excluded = codecInclusions(Collections.emptyList());
    assertThat(excluded.test("org.acme.MyCodec")).isFalse();
  }

  @Test
  public void should_include_fully_qualified_class_name() {
    Predicate<String> excluded = codecInclusions(List.of("org.acme.MyCodec"));
    assertThat(excluded.test("org.acme.MyCodec")).isTrue();
    assertThat(excluded.test("org.acme.OtherCodec")).isFalse();
    // the pattern is a class name, not a prefix
    assertThat(excluded.test("org.acme.MyCodecExtended")).isFalse();
    assertThat(excluded.test("org.acme.sub.MyCodec")).isFalse();
  }

  @Test
  public void should_reject_wildcards() {
    assertThatThrownBy(() -> codecInclusions(List.of("org.acme.codecs.*")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("org.acme.codecs.*");
  }

  @Test
  public void should_reject_pattern_without_package() {
    assertThatThrownBy(() -> codecInclusions(List.of("MyCodec")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("MyCodec");
  }
}
