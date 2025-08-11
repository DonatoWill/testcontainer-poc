package com.example.testcontainerpoc;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteSqsListener {

    private final ClienteRepository repository;
    private final ObjectMapper objectMapper;

    @SqsListener("cliente-events")
    public void listen(String message) {
        try {
            Cliente cliente = objectMapper.readValue(message, Cliente.class);
            repository.save(cliente);
            log.info("Cliente salvo via SQS: {}", cliente);
        } catch (Exception e) {
            log.error("Erro ao processar mensagem SQS", e);
        }
    }
}
