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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CustomerService {

  private final CustomerDao dao;

  @Inject
  public CustomerService(CustomerDao dao) {
    this.dao = dao;
  }

  public void create(Customer customer) {
    dao.create(customer);
  }

  public void update(Customer customer) {
    dao.update(customer);
  }

  public void delete(UUID customerId) {
    dao.delete(customerId);
  }

  public Customer findById(UUID customerId) {
    return dao.findById(customerId);
  }

  public List<Customer> findAll() {
    List<Customer> customers = new ArrayList<>();
    for (Customer customer : dao.findAll()) {
      customers.add(customer);
    }
    return customers;
  }
}
