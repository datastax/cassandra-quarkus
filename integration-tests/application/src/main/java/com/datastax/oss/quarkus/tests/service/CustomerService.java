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
package com.datastax.oss.quarkus.tests.service;

import com.datastax.oss.quarkus.tests.dao.CustomerDao;
import com.datastax.oss.quarkus.tests.entity.Customer;
import io.smallrye.mutiny.Multi;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CustomerService {

  @Inject CompletionStage<CustomerDao> daoCompletionStage;

  public CompletionStage<Void> create(Customer customer) {
    return daoCompletionStage.thenCompose(dao -> dao.create(customer));
  }

  public CompletionStage<Void> update(Customer customer) {
    return daoCompletionStage.thenCompose(dao -> dao.update(customer));
  }

  public CompletionStage<Void> delete(UUID customerId) {
    return daoCompletionStage.thenCompose(dao -> dao.delete(customerId));
  }

  public CompletionStage<Customer> findById(UUID customerId) {
    return daoCompletionStage.thenCompose(dao -> dao.findById(customerId));
  }

  public Multi<Customer> findAll() {
    return Multi.createFrom().completionStage(daoCompletionStage).flatMap(CustomerDao::findAll);
  }
}
