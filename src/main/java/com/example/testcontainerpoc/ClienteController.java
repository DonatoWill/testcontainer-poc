package com.example.testcontainerpoc;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteRepository repository;

    @PostMapping
    public ResponseEntity<Cliente> criar(@RequestBody Cliente cliente) {
        cliente.setId(UUID.randomUUID().toString());
        return ResponseEntity.ok(repository.save(cliente));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscar(@PathVariable String id) {
        Optional<Cliente> cliente = repository.findById(id);
        return cliente.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable String id, @RequestBody Cliente cliente) {
        cliente.setId(id);
        return ResponseEntity.ok(repository.save(cliente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
