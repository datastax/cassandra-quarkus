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
package com.datastax.oss.quarkus.runtime.graal;

import com.datastax.oss.driver.internal.core.os.CpuInfo;
import com.datastax.oss.driver.internal.core.os.Native;
import com.oracle.svm.core.annotate.KeepOriginal;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(Native.class)
@Substitute
final class NativeSubstitutions {

  @Substitute private static final CpuInfo.Cpu CPU = CpuInfo.determineCpu();

  @Substitute
  public static boolean isGetProcessIdAvailable() {
    return false;
  }

  @Substitute
  public static int getProcessId() {
    throw new IllegalStateException("Native operations are not supported on this platform");
  }

  @Substitute
  public static boolean isCurrentTimeMicrosAvailable() {
    return false;
  }

  @Substitute
  public static long currentTimeMicros() {
    throw new IllegalStateException("Native operations are not supported on this platform");
  }

  @KeepOriginal
  public static String getCpu() {
    return null;
  }
}
