package com.example.testcontainerpoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
public class ClienteSqsListenerTest {

    @Autowired
    SqsTemplate sqsTemplate;

    @Autowired
    ClienteRepository repository;

    @Autowired
    ObjectMapper objectMapper;

    static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.3"))
            .withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.DYNAMODB);

    @BeforeAll
    static void startContainer() {
        localstack.start();
        DynamoDbClient dynamoDb = DynamoDbClient.builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
                .region(Region.of(localstack.getRegion()))
                .credentialsProvider(() -> AwsBasicCredentials.create(
                        localstack.getAccessKey(), localstack.getSecretKey()))
                .build();

        dynamoDb.createTable(CreateTableRequest.builder()
                .tableName("Cliente")
                .keySchema(KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .provisionedThroughput(b -> b.readCapacityUnits(1L).writeCapacityUnits(1L))
                .build());

    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.credentials.access-key", localstack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localstack::getSecretKey);
        registry.add("spring.cloud.aws.region.static", localstack::getRegion);
        registry.add("spring.cloud.aws.sqs.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
        registry.add("spring.cloud.aws.dynamodb.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
    }

    @Test
    public void listenerShouldSaveCliente() throws Exception {

        var id = UUID.randomUUID().toString();

        Cliente cliente = Cliente.builder()
                .id(id)
                .nome("SQS User")
                .email("sqs@example.com")
                .build();

        String message = objectMapper.writeValueAsString(cliente);

        sqsTemplate.send(to -> to.queue("cliente-events").payload(message));

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(repository.findById(id)).isPresent());
    }

}