package com.example.testcontainerpoc;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@WireMockTest(httpPort = 9000)
@ImportAutoConfiguration(FeignAutoConfiguration.class)
class ExternalClientTest {

    @Autowired
    ExternalClient externalClient;

    static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.3"))
            .withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.DYNAMODB);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.credentials.access-key", localstack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localstack::getSecretKey);
        registry.add("spring.cloud.aws.region.static", localstack::getRegion);
        registry.add("spring.cloud.aws.sqs.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
        registry.add("spring.cloud.aws.dynamodb.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
        registry.add("external.api.url", () -> "http://localhost:" + 9000);
    }

    @BeforeAll
    static void startContainer() {
        localstack.start();
    }

    @Test
    public void testGetExternalData() {
            stubFor(get(urlEqualTo("/external-api/data"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody("Expected Data")));
        String response = externalClient.getExternalData();
        assertNotNull(response);
        assertEquals("Expected Data", response); // Adjust based on your expectations
    }
}