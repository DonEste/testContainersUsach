package com.example.test_containers_usach;

import com.example.test_containers_usach.postgres.Product;
import com.example.test_containers_usach.postgres.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Annotation for JPA tests, setting up an in-memory DB by default, but we'll override it
@Testcontainers // Enables Testcontainers JUnit 5 integration
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// Crucial: tells Spring Boot NOT to replace our Testcontainers DB
@ContextConfiguration(initializers = PostgresIntegrationTest.PostgresContainerInitializer.class)
// Link to our initializer

public class PostgresIntegrationTest {

    // Define the PostgreSQL container
    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        // Optional: Ensure a clean state before each test if not using @Transactional
        productRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // Optional: Clean up after each test (usually handled by @Transactional or deleteAll in BeforeEach)
    }

    @Test
    void testSaveAndFindProduct() {
        Product product = new Product("Laptop", 1200.00);
        Product savedProduct = productRepository.save(product);

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Laptop");

        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Laptop");
    }

    @Test
    void testFindAllProducts() {
        productRepository.save(new Product("Keyboard", 75.00));
        productRepository.save(new Product("Mouse", 25.00));

        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName).containsExactlyInAnyOrder("Keyboard", "Mouse");
    }

    @Test
    void testUpdateProduct() {
        Product product = productRepository.save(new Product("Monitor", 300.00));
        product.setPrice(350.00);
        Product updatedProduct = productRepository.save(product);

        assertThat(updatedProduct.getPrice()).isEqualTo(350.00);
        Optional<Product> foundProduct = productRepository.findById(updatedProduct.getId());
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getPrice()).isEqualTo(350.00);
    }

    @Test
    void testDeleteProduct() {
        Product product = productRepository.save(new Product("Webcam", 50.00));
        Long productId = product.getId();

        productRepository.deleteById(productId);

        Optional<Product> foundProduct = productRepository.findById(productId);
        assertThat(foundProduct).isNotPresent();
    }

    /**
     * This initializer dynamically sets Spring Boot properties to connect to the Testcontainers PostgreSQL instance.
     */
    static class PostgresContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgresContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresContainer.getUsername(),
                    "spring.datasource.password=" + postgresContainer.getPassword(),
                    "spring.datasource.driver-class-name=" + postgresContainer.getDriverClassName(),
                    "spring.jpa.hibernate.ddl-auto=create-drop" // Important for tests: creates schema and drops it after
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
