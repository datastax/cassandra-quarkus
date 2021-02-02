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
package com.datastax.oss.quarkus.internal.mapper.processor;

import com.datastax.oss.driver.internal.mapper.processor.DecoratedMessager;
import com.datastax.oss.driver.internal.mapper.processor.MapperProcessor;
import com.datastax.oss.driver.internal.mapper.processor.ProcessorContext;
import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class QuarkusMapperProcessor extends MapperProcessor {

  @Override
  protected ProcessorContext buildContext(
      DecoratedMessager messager,
      Types typeUtils,
      Elements elementUtils,
      Filer filer,
      String indent,
      boolean logsEnabled,
      boolean customResultsEnabled) {
    return new QuarkusProcessorContext(
        messager, typeUtils, elementUtils, filer, indent, logsEnabled, customResultsEnabled);
  }
}
