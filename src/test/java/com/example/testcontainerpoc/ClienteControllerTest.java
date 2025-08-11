package com.example.testcontainerpoc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClienteControllerTest {

    @Autowired
    TestRestTemplate restTemplate;

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
    void criarBuscarAtualizarDeletarCliente() {

        Cliente novo = Cliente.builder().nome("Fulano").email("fulano@email.com").build();
        ResponseEntity<Cliente> postResponse = restTemplate.postForEntity("/clientes", novo, Cliente.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Cliente criado = postResponse.getBody();
        assertThat(criado).isNotNull();
        assertThat(criado.getId()).isNotBlank();

        ResponseEntity<Cliente> getResponse = restTemplate.getForEntity("/clientes/" + criado.getId(), Cliente.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getNome()).isEqualTo("Fulano");

        Cliente atualizado = Cliente.builder().nome("Beltrano").email("beltrano@email.com").build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Cliente> putRequest = new HttpEntity<>(atualizado, headers);
        ResponseEntity<Cliente> putResponse = restTemplate.exchange("/clientes/" + criado.getId(), HttpMethod.PUT, putRequest, Cliente.class);

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isNotNull();
        assertThat(putResponse.getBody().getNome()).isEqualTo("Beltrano");

        ResponseEntity<Void> deleteResponse = restTemplate.exchange("/clientes/" + criado.getId(), HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Cliente> getDeleted = restTemplate.getForEntity("/clientes/" + criado.getId(), Cliente.class);
        assertThat(getDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}