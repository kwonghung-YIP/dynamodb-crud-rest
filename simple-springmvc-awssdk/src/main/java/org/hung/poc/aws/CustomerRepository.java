package org.hung.poc.aws;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    public Optional<Customer> findById(UUID id);

    public List<Customer> findAll();

    public Customer create(Customer customer);

    public Customer updated(Customer customer);

    public Customer deleteById(UUID id);
    
}
