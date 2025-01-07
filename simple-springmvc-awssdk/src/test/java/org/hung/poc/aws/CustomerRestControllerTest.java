package org.hung.poc.aws;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class CustomerRestControllerTest {

	@Container
	static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:4.0.3"));

	@Autowired 
	private TestRestTemplate restTemplate;

	private DynamoDbTable<Customer> table;

	@DynamicPropertySource
	static void loadProperties(DynamicPropertyRegistry registry) {
		log.info("endpoint-override:{}",localstack.getEndpointOverride(Service.DYNAMODB).toString());

		registry.add("aws.endpoint.override.dynamodb",() -> localstack.getEndpointOverride(Service.DYNAMODB).toString());
	}

	@BeforeAll
	static void beforeAll() throws IOException, InterruptedException {
		log.info("access-key:{}",localstack.getAccessKey());
		log.info("secret-key:{}",localstack.getSecretKey());

		System.setProperty("aws.accessKeyId", localstack.getAccessKey());
		System.setProperty("aws.secretAccessKey", localstack.getSecretKey());

		ExecResult result = localstack.execInContainer("awslocal","dynamodb",
			"create-table","--table-name","customer",
			"--key-schema","AttributeName=id,KeyType=HASH",
			"--attribute-definitions","AttributeName=id,AttributeType=S",
			"--billing-mode","PAY_PER_REQUEST");

		log.info("{} {} {}", result.getExitCode(), result.getStdout(), result.getStderr());
	}

	@BeforeEach
	void beforeEach(@Autowired DynamoDbEnhancedClient client) {
		table = client.table("customer", TableSchema.fromClass(Customer.class));
	}

	@Test
	void findById() {
		Customer entity = new Customer();
		entity.setId(UUID.randomUUID());
		entity.setLastName("Doe");
		entity.setFirstName("John");
		entity.setEmail("john.doe@gmail.com");
		table.putItem(entity);

		ResponseEntity<Customer> response = restTemplate.getForEntity("/customer/{id}", Customer.class, entity.getId());

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getId()).isEqualTo(entity.getId());
		assertThat(response.getBody().getLastName()).isEqualTo("Doe");
		assertThat(response.getBody().getFirstName()).isEqualTo("John");
		assertThat(response.getBody().getEmail()).isEqualTo("john.doe@gmail.com");
	}

	@Test
	void findById_with_error() {
		ResponseEntity<ProblemDetail> response = restTemplate.getForEntity("/customer/{id}", ProblemDetail.class, UUID.randomUUID());
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void create() {
		Customer entity = new Customer();
		entity.setLastName("Doe");
		entity.setFirstName("Jane");
		entity.setEmail("jane.doe@gmail.com");

		ResponseEntity<Customer> response = restTemplate.postForEntity("/customer/", entity, Customer.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		Key key = Key.builder().partitionValue(response.getBody().getId().toString()).build();
		Customer actual = table.getItem(key);

		assertThat(actual.getId()).isEqualTo(response.getBody().getId());
		assertThat(actual.getLastName()).isEqualTo("Doe");
		assertThat(actual.getFirstName()).isEqualTo("Jane");
		assertThat(actual.getEmail()).isEqualTo("jane.doe@gmail.com");
	}

	@Test
	void update() {
		Customer entity = new Customer();
		entity.setId(UUID.randomUUID());
		entity.setLastName("Peter");
		entity.setFirstName("Lee");
		entity.setEmail("peter.lee@gmail.com");

		table.putItem(entity);

		entity.setEmail("peter.lee@yahoo.com");

		restTemplate.put("/customer/{id}", entity, entity.getId());

		Key key = Key.builder().partitionValue(entity.getId().toString()).build();
		Customer actual = table.getItem(key);

		assertThat(actual.getEmail()).isEqualTo("peter.lee@yahoo.com");
	}

	@Test
	void delete() {
		Customer entity = new Customer();
		entity.setId(UUID.randomUUID());
		entity.setLastName("David");
		entity.setFirstName("Simspon");
		entity.setEmail("david.simspon@gmail.com");

		table.putItem(entity);

		restTemplate.delete("/customer/{id}", entity.getId());

		Key key = Key.builder().partitionValue(entity.getId().toString()).build();
		Customer actual = table.getItem(key);

		assertThat(actual).isNull();
	}

}
