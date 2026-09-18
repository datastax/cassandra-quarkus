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

import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import io.quarkus.runtime.configuration.ConfigurationException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Predicate;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

public class CodecRegistrationSupport {

  static final DotName TYPE_CODEC = DotName.createSimple(TypeCodec.class.getName());

  static final DotName CODEC_PROVIDER_ANNOTATION =
      DotName.createSimple("com.datastax.oss.quarkus.runtime.api.config.CassandraCodecProvider");

  private static final String PACKAGE_WILDCARD = ".*";

  private static final String SUBPACKAGES_WILDCARD = ".**";

  private CodecRegistrationSupport() {}

  /**
   * Turns the {@code quarkus.cassandra.codecs.exclude} entries into a predicate that tells whether
   * a codec class name is excluded.
   *
   * <p>Each entry is one of:
   *
   * <ul>
   *   <li>a fully qualified class name, i.e. {@code org.acme.MyCodec}, which excludes that codec;
   *   <li>a package name suffixed with {@code .*}, i.e. {@code org.acme.*}, which excludes the
   *       codecs of that package;
   *   <li>a package name suffixed with {@code .**}, i.e. {@code org.acme.**}, which excludes the
   *       codecs of that package and of all its subpackages.
   * </ul>
   *
   * <p>This is the same syntax as {@code quarkus.arc.exclude-types}, minus the simple class name
   * form: an entry without a dot would silently match nothing here, so it is rejected instead.
   */
  static Predicate<String> codecExclusions(List<String> patterns) {
    List<Predicate<String>> exclusions =
        patterns.stream()
            .map(String::trim)
            .filter(pattern -> !pattern.isEmpty())
            .map(CodecRegistrationSupport::toCodecExclusion)
            .toList();
    return codecClassName -> exclusions.stream().anyMatch(e -> e.test(codecClassName));
  }

  /**
   * Turns the {@code quarkus.cassandra.codecs.include} entries into a predicate that tells whether
   * a codec class name is included.
   *
   * <p>In contrast to exclusions, this only supports fully qualified class names.
   */
  static Predicate<String> codecInclusions(List<String> fqns) {
    List<Predicate<String>> inclusions =
        fqns.stream()
            .map(String::trim)
            .filter(fqn -> !fqn.isEmpty())
            .map(CodecRegistrationSupport::toCodecInclusion)
            .toList();
    return codecClassName -> inclusions.stream().anyMatch(e -> e.test(codecClassName));
  }

  private static Predicate<String> toCodecInclusion(String fqn) {
    if (fqn.endsWith(PACKAGE_WILDCARD) || fqn.indexOf('.') < 0) {
      throw new ConfigurationException(
          String.format(
              "Invalid entry in quarkus.cassandra.codecs.include: '%s'. Expecting a fully qualified class name.",
              fqn));
    }

    return codecClassName -> codecClassName.equals(fqn);
  }

  private static Predicate<String> toCodecExclusion(String pattern) {
    if (pattern.endsWith(SUBPACKAGES_WILDCARD)) {
      String root = pattern.substring(0, pattern.length() - SUBPACKAGES_WILDCARD.length());
      return codecClassName -> {
        String pkg = packageName(codecClassName);
        return pkg.equals(root) || pkg.startsWith(root + ".");
      };
    }
    if (pattern.endsWith(PACKAGE_WILDCARD)) {
      String pkg = pattern.substring(0, pattern.length() - PACKAGE_WILDCARD.length());
      return codecClassName -> packageName(codecClassName).equals(pkg);
    }
    if (pattern.indexOf('.') < 0) {
      throw new ConfigurationException(
          String.format(
              "Invalid entry in quarkus.cassandra.codecs.exclude: '%s'. Expecting a fully "
                  + "qualified class name, or a package name suffixed with '.*' or '.**'.",
              pattern));
    }
    return codecClassName -> codecClassName.equals(pattern);
  }

  private static String packageName(String className) {
    int lastDot = className.lastIndexOf('.');
    return lastDot < 0 ? "" : className.substring(0, lastDot);
  }

  static boolean isDriverClass(String className) {
    return className.startsWith("com.datastax.oss.driver.")
        || className.startsWith("com.datastax.dse.driver.");
  }

  static boolean hasPublicNoArgConstructor(ClassInfo clz) {
    return clz.constructors().stream()
        .anyMatch(
            constructor ->
                constructor.parametersCount() == 0 && Modifier.isPublic(constructor.flags()));
  }

  static boolean isCodecProviderMethod(MethodInfo method) {
    return Modifier.isPublic(method.flags())
        && Modifier.isStatic(method.flags())
        && method.parametersCount() == 0
        && isCodecArray(method.returnType());
  }

  private static boolean isCodecArray(Type type) {
    return type.kind() == Type.Kind.ARRAY
        && type.asArrayType().dimensions() == 1
        && type.asArrayType().constituent().name().equals(TYPE_CODEC);
  }
}
