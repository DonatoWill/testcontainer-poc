package com.example.testcontainerpoc;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;

import java.util.Optional;

@Repository
public class ClienteRepository {

    private final DynamoDbTable<Cliente> table;

    public ClienteRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("Cliente", TableSchema.fromBean(Cliente.class));
    }

    public Cliente save(Cliente cliente) {
        table.putItem(cliente);
        return cliente;
    }

    public Optional<Cliente> findById(String id) {
        return Optional.ofNullable(table.getItem(r -> r.key(k -> k.partitionValue(id))));
    }

    public void deleteById(String id) {
        table.deleteItem(r -> r.key(k -> k.partitionValue(id)));
    }
}
