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

import static com.datastax.oss.quarkus.deployment.internal.CodecRegistrationSupport.codecExclusions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.quarkus.runtime.configuration.ConfigurationException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

/** Tests the {@code quarkus.cassandra.codecs.exclude} pattern syntax. */
public class CodecExclusionsTest {

  @Test
  public void should_exclude_nothing_when_no_pattern_configured() {
    Predicate<String> excluded = codecExclusions(Collections.emptyList());
    assertThat(excluded.test("org.acme.MyCodec")).isFalse();
  }

  @Test
  public void should_exclude_fully_qualified_class_name() {
    Predicate<String> excluded = codecExclusions(List.of("org.acme.MyCodec"));
    assertThat(excluded.test("org.acme.MyCodec")).isTrue();
    assertThat(excluded.test("org.acme.OtherCodec")).isFalse();
    // the pattern is a class name, not a prefix
    assertThat(excluded.test("org.acme.MyCodecExtended")).isFalse();
    assertThat(excluded.test("org.acme.sub.MyCodec")).isFalse();
  }

  @Test
  public void should_exclude_package_with_single_wildcard() {
    Predicate<String> excluded = codecExclusions(List.of("org.acme.codecs.*"));
    assertThat(excluded.test("org.acme.codecs.MyCodec")).isTrue();
    // nested classes belong to the package of their enclosing class
    assertThat(excluded.test("org.acme.codecs.Outer$MyCodec")).isTrue();
    // a single wildcard stops at the package boundary
    assertThat(excluded.test("org.acme.codecs.sub.MyCodec")).isFalse();
    assertThat(excluded.test("org.acme.MyCodec")).isFalse();
  }

  @Test
  public void should_exclude_package_and_subpackages_with_double_wildcard() {
    Predicate<String> excluded = codecExclusions(List.of("org.acme.codecs.**"));
    assertThat(excluded.test("org.acme.codecs.MyCodec")).isTrue();
    assertThat(excluded.test("org.acme.codecs.sub.MyCodec")).isTrue();
    assertThat(excluded.test("org.acme.codecs.sub.deeper.MyCodec")).isTrue();
    assertThat(excluded.test("org.acme.MyCodec")).isFalse();
    // the wildcard matches whole package segments, not arbitrary prefixes
    assertThat(excluded.test("org.acme.codecsx.MyCodec")).isFalse();
  }

  @Test
  public void should_exclude_codec_matching_any_of_the_patterns() {
    Predicate<String> excluded =
        codecExclusions(
            Arrays.asList("org.acme.MyCodec", "org.acme.codecs.*", " org.acme.legacy.** ", ""));
    assertThat(excluded.test("org.acme.MyCodec")).isTrue();
    assertThat(excluded.test("org.acme.codecs.OtherCodec")).isTrue();
    assertThat(excluded.test("org.acme.legacy.sub.OldCodec")).isTrue();
    assertThat(excluded.test("org.acme.kept.KeptCodec")).isFalse();
  }

  @Test
  public void should_reject_pattern_without_package() {
    assertThatThrownBy(() -> codecExclusions(List.of("MyCodec")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("MyCodec");
  }
}
