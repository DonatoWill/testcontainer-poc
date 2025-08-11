package com.example.testcontainerpoc;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainers {

//    static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.3"))
//            .withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.DYNAMODB);
//
//    @DynamicPropertySource
//    static void registerProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.cloud.aws.credentials.access-key", localstack::getAccessKey);
//        registry.add("spring.cloud.aws.credentials.secret-key", localstack::getSecretKey);
//        registry.add("spring.cloud.aws.region.static", localstack::getRegion);
//        registry.add("spring.cloud.aws.sqs.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
//        registry.add("spring.cloud.aws.dynamodb.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
//        registry.add("external.api.url", () -> "http://localhost:" + 9000);
//    }
//
//    @BeforeAll
//    static void startContainer() {
//        localstack.start();
//    }

}
