package com.example.testcontainerpoc;

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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ClienteRepositoryTest {

    static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.3"))
            .withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.DYNAMODB);


    @Autowired
    ClienteRepository repository;

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
        registry.add("spring.cloud.aws.dynamodb.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
        registry.add("spring.cloud.aws.sqs.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());

    }

    @Test
    public void saveAndFindById() {
        Cliente cliente = Cliente.builder()
                .id(UUID.randomUUID().toString())
                .nome("Test User")
                .email("test@example.com")
                .build();

        repository.save(cliente);

        assertThat(repository.findById(cliente.getId()))
                .isPresent()
                .get()
                .extracting(Cliente::getNome)
                .isEqualTo("Test User");
    }
}