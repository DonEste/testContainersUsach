# Read Me First
The following was discovered as part of building this project:

* The original package name 'com.example.test-containers-usach' is invalid and this project uses 'com.example.test_containers_usach' instead.

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.7-SNAPSHOT/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.4.7-SNAPSHOT/maven-plugin/build-image.html)
* [Spring Boot Testcontainers support](https://docs.spring.io/spring-boot/3.4.7-SNAPSHOT/reference/testing/testcontainers.html#testing.testcontainers)
* [Testcontainers GCloud Module Reference Guide](https://java.testcontainers.org/modules/gcloud/)
* [Testcontainers Postgres Module Reference Guide](https://java.testcontainers.org/modules/databases/postgres/)
* [Testcontainers](https://java.testcontainers.org/)
* [Google Cloud Messaging](https://googlecloudplatform.github.io/spring-cloud-gcp/reference/html/index.html#cloud-pubsub)

### Guides
The following guides illustrate how to use some features concretely:

* [Google Cloud Pub/Sub Sample](https://github.com/GoogleCloudPlatform/spring-cloud-gcp/tree/main/spring-cloud-gcp-samples/spring-cloud-gcp-pubsub-sample)

### Testcontainers support

This project uses [Testcontainers at development time](https://docs.spring.io/spring-boot/3.4.7-SNAPSHOT/reference/features/dev-services.html#features.dev-services.testcontainers).

Testcontainers has been configured to use the following Docker images:

* [`postgres:latest`](https://hub.docker.com/_/postgres)

Please review the tags of the used images and set them to the same as you're running in production.

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

