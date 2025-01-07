package org.hung.poc.aws;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

@Repository
@Slf4j
@RequiredArgsConstructor
public class CustomerRepoDynamodbImpl implements CustomerRepository {

    private final DynamoDbEnhancedClient client;
    private DynamoDbTable<Customer> table;

    @PostConstruct
    void setup() {
        table = client.table("customer", TableSchema.fromClass(Customer.class));
    }

    @Override
    public Optional<Customer> findById(UUID id) {    
        return Optional.ofNullable(table.getItem(uuid2Key(id)));
    }

    @Override
    public List<Customer> findAll() {
        PageIterable<Customer> results = table.scan();
        return results.items().stream().collect(Collectors.toList());
    }

    @Override
    public Customer create(Customer customer) {
        customer.setId(UUID.randomUUID());
        table.putItem(customer);
        return customer;
    }

    @Override
    public Customer updated(Customer customer) {
        return table.updateItem(customer);
    }

    @Override
    public Customer deleteById(UUID id) {
        return table.deleteItem(uuid2Key(id));
    }

    private Key uuid2Key(UUID id) {
        return Key.builder().partitionValue(id.toString()).build();
    }

}
