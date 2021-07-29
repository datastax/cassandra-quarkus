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
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.PropertyStrategy;
import java.util.Objects;
import java.util.UUID;

@Entity
@PropertyStrategy(mutable = false)
public class Votes {

  @PartitionKey private final UUID productId;

  private final long upVotes;

  private final long downVotes;

  public Votes(UUID productId, long upVotes, long downVotes) {
    this.productId = productId;
    this.upVotes = upVotes;
    this.downVotes = downVotes;
  }

  public UUID getProductId() {
    return productId;
  }

  public long getUpVotes() {
    return upVotes;
  }

  public long getDownVotes() {
    return downVotes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Votes)) {
      return false;
    }
    Votes votes = (Votes) o;
    return upVotes == votes.upVotes
        && downVotes == votes.downVotes
        && productId.equals(votes.productId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productId, upVotes, downVotes);
  }
}
