# selfcare-institition-ms

## Description
This Spring Boot-based microservice is designed to handle several key functionalities in the selfcare operations domain. It includes business logic for:

- Onboarding operations.
- Management of institutions and delegations.
- User creation.
- Associating users with products and institutions.

## Prerequisites
Before running the microservice, ensure you have installed:

- Java JDK 17 or higher
- Maven 3.6 or higher
- Connection to VPN selc-d-vnet

## Configuration
Look at app/src/main/resources/`application.yml` file to set up environment-specific settings, such as database details.

## Installation and Local Startup
To run the microservice locally, follow these steps:

1. **Clone the Repository**

```shell script
git clone https://github.com/pagopa/selfcare-institution.git
cd apps/selfcare-institution-ms/
```

2. **Build the Project**

```shell script
mvn clean install
```

2. **Start the Application**

```shell script
mvn spring-boot:run -pl app
```

## Usage
After starting, the microservice will be available at `http://localhost:8080/`.

To use the API, refer to the Swagger UI documentation (if available) at `http://localhost:8080/swagger-ui.html`.

## Integration tests
A new suite of integration tests written with cucumber was added in `it.pagopa.selfcare.mscore.integration_test` package.
The tests are currently disabled by default, to run the tests locally:

1. Open the docker-compose.yml file located in the root of institution-ms and replace the placeholder REPLACE_WITH_TOKEN with a valid GitHub PAT.
2. Start mongodb, azurite and mockserver using the docker-compose.yml in this folder:
    ```shell script
    docker-compose up mongodb
    docker-compose up azurite
    docker-compose up mockserver
    ```

3. Run user-ms at port 8082 using the test public key in `src/test/resources/key/public-key.pub`
   (by setting the value in a single line of the env var JWT-PUBLIC-KEY).
   The institution-ms microservice is started automatically with the CucumberSuite

4. Comment the line starting with @ExcludeTags inside the CucumberSuite file and run the test with maven:
    ```shell script
   mvn test -DfailIfNoTests=false -Dtest=it.pagopa.selfcare.mscore.integration_test.CucumberSuite
   ```
