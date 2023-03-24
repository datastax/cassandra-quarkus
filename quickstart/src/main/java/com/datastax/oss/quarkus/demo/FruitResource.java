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
package com.datastax.oss.quarkus.demo;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A REST resource exposing endpoints for creating and retrieving {@link Fruit} objects in the
 * database, leveraging the {@link FruitService} component.
 */
@Path("/fruits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FruitResource {

  @Inject FruitService fruitService;

  @GET
  public List<FruitDto> getAll() {
    return fruitService.getAll().stream().map(this::convertToDto).collect(Collectors.toList());
  }

  @POST
  public void add(FruitDto fruit) {
    fruitService.save(convertFromDto(fruit));
  }

  private FruitDto convertToDto(Fruit fruit) {
    return new FruitDto(fruit.getName(), fruit.getDescription());
  }

  private Fruit convertFromDto(FruitDto fruitDto) {
    return new Fruit(fruitDto.getName(), fruitDto.getDescription());
  }
}
