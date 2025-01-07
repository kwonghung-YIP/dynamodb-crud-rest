## Features covered in this example

- This example implements a simple CRUD REST with Spring Boot MVC back with AWS DynamoDB.
- To keep it simple and have better understanding the AWS DynamoDB service, the example uses (AWS SDK for Java 2.x)[https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html] directly but not using (Spring Cloud for Amazon Web Service)[https://spring.io/projects/spring-cloud-aws]
- Instead of developing on AWS cloud, (LocalStack)[https://hub.docker.com/r/localstack/localstack] is used for local development, and also for JUnit test with TestContainer
- DynamoDB Enhanced Client is used for mapping between Java POJO/JSON and DynamoDB item

## Set up LocalStack with docker-compose
```bash
cd localstack

docker compose up
```

## Connect LocalStack with AWS CLI
```bash
cd localstack

docker run --name awscli --rm -it \
    --network localstack_default \
    --link localstack-main \
    --entrypoint /bin/bash \
    -v "./.aws:/root/.aws" \
    -v "./dynamodb:/root/dynamodb" \
    -w "/root" \
    -e AWS_PROFILE=localstack \
    amazon/aws-cli:2.22.12
```

## Create DynamoDB table and other related operations
[AWS CLI 2.x Command Reference - dynamodb](https://awscli.amazonaws.com/v2/documentation/api/latest/reference/dynamodb/index.html)
```bash
aws dynamodb create-table \
    --table-name customer \
    --key-schema \
        AttributeName=id,KeyType=HASH \
    --attribute-definitions \
        AttributeName=id,AttributeType=S \
    --billing-mode PAY_PER_REQUEST

aws dynamodb list-tables

aws dynamodb describe-table \
    --table-name customer

aws dynamodb put-item \
    --table-name customer \
    --item "file://./dynamodb/customer-items.json"

aws dynamodb get-item \
    --table-name customer \
    --key 'file://./dynamodb/customer-key.json'

aws dynamodb scan \
    --table-name customer

aws dynamodb query \
    --table-name customer \
    --key-condition-expression "id=:999"

aws dynamodb delete-table \
    --table-name customer
```

## curl commands for testing the REST API
```bash
curl -X POST \
    -H "Content-Type: application/json" \
    -d '{"firstName":"John","lastName":"Doe","email":"john.doe@yagoo.com"}' \
    http://localhost:8080/customer/

curl -X POST \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Jane","lastName":"Doe","email":"jane.doe@yagoo.com"}' \
    http://localhost:8080/customer/

curl -X POST \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Peter","lastName":"Bistop","email":"peter.bistop@yagoo.com"}' \
    http://localhost:8080/customer/

curl -X GET \
    http://localhost:8080/customer/

curl -X GET \
    http://localhost:8080/customer/7255d7dd-fc42-4d72-a4ed-b62302b83c87

curl -X PUT \
    -H "Content-Type: application/json" \
    -d '{"id":"7255d7dd-fc42-4d72-a4ed-b62302b83c87","firstName":"Jane","lastName":"Doe","email":"jane.doe@gmail.com"}' \
    http://localhost:8080/customer/7255d7dd-fc42-4d72-a4ed-b62302b83c87

curl -X DELETE \
    http://localhost:8080/customer/79886e6a-d430-4f2c-a691-3e74d56fb994
```

## Reference
- [Testing AWS service integrations using LocalStack](https://testcontainers.com/guides/testing-aws-service-integrations-using-localstack/)
- [Developer Guide - AWS SDK for Java 2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)
- [Learn the basics of the DynamoDB Enhanced Client API](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/ddb-en-client-use.html)
- [ClassCastExcpetion while DynamoDB.putItem](https://stackoverflow.com/questions/61556638/springboot-java-aws-sdk-2-dynamodb-enhanced-client-and-devtools-problem/64350067#64350067)