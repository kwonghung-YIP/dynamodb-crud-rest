package org.hung.poc.aws;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class AwsConfiguration {

    @Value("${aws.endpoint.override.dynamodb:http://localhost:4566}")
    private URI defaultEndpoint;

    @Value("${aws.region:US_EAST_1}")
    private Region defaultRegion;
    
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient() {
        DynamoDbClient standard = DynamoDbClient.builder()
            .region(defaultRegion)
            // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
            .credentialsProvider(DefaultCredentialsProvider.create())
            .applyMutation(builder -> {
                builder.endpointOverride(defaultEndpoint);
            })
            .build();

            DynamoDbEnhancedClient enhanced = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(standard)
            .build();

        return enhanced;
    }
}
