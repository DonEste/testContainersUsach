 #Spring Boot GCP Pub/Sub & Testcontainers Example

This project provides a comprehensive example of building a **Spring Boot** application that interacts with **Google Cloud Pub/Sub**, showcasing robust **unit and integration testing strategies** using **Testcontainers** and **Mockito**.

---

## Features

* **Spring Boot Application**: A basic application demonstrating product management (CRUD operations).
* **Google Cloud Pub/Sub Integration**:
    * `PubSubService`: For publishing messages to a Pub/Sub topic.
    * `PubSubListener`: For consuming messages from a Pub/Sub subscription.
* **Comprehensive Testing**:
    * **Unit Testing** with **Mockito**: Isolates service layers from their dependencies.
    * **Integration Testing** with **Testcontainers**:
        * Utilizes a **PostgreSQL container** for JPA/Hibernate database interactions.
        * Employs a **Google Cloud Pub/Sub Emulator container** for testing Pub/Sub message flows.

---

## Technologies Used

* **Java 17+**
* **Spring Boot 3+**
* **Spring Data JPA**
* **Google Cloud Pub/Sub Spring Starter**
* **Lombok**: Reduces boilerplate code.
* **JUnit 5**: Testing framework.
* **Mockito**: Mocking framework for unit tests.
* **AssertJ**: Fluent assertions for tests.
* **Awaitility**: Simplifies asynchronous testing.
* **Testcontainers**: For managing Docker containers in integration tests, including:
    * `PostgreSQLContainer`
    * `PubSubEmulatorContainer`

---

## Project Structure Highlights

* `src/main/java/com/example/Product.java`: JPA Entity representing a product.
* `src/main/java/com/example/ProductRepository.java`: Spring Data JPA Repository for `Product` entity.
* `src/main/java/com/example/ProductService.java`: Business logic for product operations.
* `src/main/java/com/example/PubSubService.java`: Handles publishing messages to Pub/Sub.
* `src/main/java/com/example/PubSubListener.java`: Consumes messages from Pub/Sub.
* `src/test/java/com/example/test_containers_usach/PostgresIntegrationTest.java`: Integration tests for `ProductService` using a **Testcontainers PostgreSQL**.
* `src/test/java/com/example/test_containers_usach/PostgresServiceMockTest.java`: Unit tests for `ProductService` using **Mockito**.
* `src/test/java/com/example/test_containers_usach/PubSubIntegrationTest.java`: Integration tests for the Pub/Sub message flow using a **Testcontainers Pub/Sub Emulator**.
* `src/test/java/com/example/test_containers_usach/PubSubServiceMockTest.java`: Unit tests for `PubSubService` using **Mockito**.

---

## How to Run

1.  **Prerequisites**: Ensure **Docker** is installed and running on your machine.
2.  **Clone the repository**:
    ```bash
    git clone <repository-url>
    ```
3.  **Navigate to the project directory**:
    ```bash
    cd spring-boot-gcp-pubsub-testcontainers-example
    ```
4.  **Build the project and run tests**:
    ```bash
    mvn clean install
    ```
    This command will execute all unit and integration tests. Testcontainers will automatically spin up the necessary Docker containers (PostgreSQL and Pub/Sub Emulator) during the integration test phases.

---

## Testing Highlights

This project demonstrates a robust testing pyramid:

* ### `PostgresIntegrationTest.java`
    This class showcases **integration testing for your data layer**. It uses **Testcontainers** to spin up a real **PostgreSQL database** in a Docker container. Your Spring Data JPA repositories and `ProductService` will interact with this actual database, ensuring that your entity mappings, queries, and transaction management work as expected in a near-production environment.

* ### `PostgresServiceMockTest.java`
    This class focuses on **unit testing the `ProductService`**. It uses **Mockito** to mock the `ProductRepository` dependency, preventing any actual database interaction. This allows for fast, isolated tests that verify the business logic within `ProductService` without the overhead of a database.

* ### `PubSubIntegrationTest.java`
    This test suite provides an **end-to-end integration test for the Pub/Sub message flow**. It leverages **Testcontainers** to launch a **Google Cloud Pub/Sub Emulator** within a Docker container. Your `PubSubService` will publish messages to this local emulator, and your `PubSubListener` will consume them, simulating the complete Pub/Sub interaction without connecting to Google Cloud.

* ### `PubSubServiceMockTest.java`
    This class performs **unit testing for the `PubSubService`**. It employs **Mockito** to mock the `PubSubTemplate` (the client responsible for interacting with Pub/Sub). This allows you to verify that `PubSubService` correctly transforms and attempts to publish messages, without needing a live Pub/Sub instance or emulator.