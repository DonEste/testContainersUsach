package com.example.test_containers_usach;

import com.example.Product;
import com.example.ProductRepository;
import com.example.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Integration Test** for JPA/Hibernate with a **Testcontainers-managed PostgreSQL database**.
 *
 * This test uses `DataJpaTest` to focus on JPA components (repositories and entities)
 * and `ProductService` by explicitly including it. It ensures that the application's
 * data layer interacts correctly with a real PostgreSQL database instance.
 */
@DataJpaTest( // Configures Spring Boot to test JPA components.
        // Specifies to include ProductService in the test context, even though DataJpaTest usually scans only repositories.
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {ProductService.class}
        ))
@Testcontainers // Enables Testcontainers for automatic container lifecycle management.
// Prevents DataJpaTest from replacing the actual DataSource with an in-memory one (like H2).
// This forces it to use the database provided by Testcontainers.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// Attaches an initializer to dynamically configure Spring properties for the PostgreSQL container.
@ContextConfiguration(initializers = PostgresIntegrationTest.PostgresContainerInitializer.class)
public class PostgresIntegrationTest {

    // --- Testcontainers PostgreSQL Container ---
    // Declares a PostgreSQL container; Testcontainers manages its start/stop.
    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb").withUsername("testuser").withPassword("testpass");

    // --- Autowired Application Components ---
    @Autowired
    private ProductService productService; // The service under test, which uses the repository.

    @Autowired
    private ProductRepository productRepository; // The JPA repository for direct database interaction/setup.

    /**
     * Cleans up the database before each test. Ensures test isolation.
     */
    @BeforeEach
    void setUp() {
        productRepository.deleteAll(); // Clears all data from the product table.
    }

    /**
     * Tests saving a product and then retrieving it by ID.
     */
    @Test
    void testSaveAndFindProduct() {
        Product product = new Product(null, "Laptop", 1200.00);
        Product savedProduct = productService.createProduct(product); // Uses the service to create.

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull(); // Verifies ID was generated.
        assertThat(savedProduct.getName()).isEqualTo("Laptop");

        Optional<Product> foundProduct = productService.getProductById(savedProduct.getId()); // Retrieves by ID.
        assertThat(foundProduct).isPresent(); // Confirms product was found.
        assertThat(foundProduct.get().getName()).isEqualTo("Laptop");
    }

    /**
     * Tests retrieving all products from the database.
     */
    @Test
    void testFindAllProducts() {
        productService.createProduct(new Product(null, "Keyboard", 75.00));
        productService.createProduct(new Product(null, "Mouse", 25.00));

        List<Product> products = productService.getAllProducts(); // Gets all products.
        assertThat(products).hasSize(2); // Verifies the correct number of products.
        assertThat(products).extracting(Product::getName).containsExactlyInAnyOrder("Keyboard", "Mouse"); // Verifies names.
    }

    /**
     * Tests updating an existing product's details.
     */
    @Test
    void testUpdateProduct() {
        Product product = productService.createProduct(new Product(null, "Monitor", 300.00));
        Product updatedDetails = new Product(null, "New Monitor", 350.00);

        Product updatedProduct = productService.updateProduct(product.getId(), updatedDetails); // Updates via service.

        assertThat(updatedProduct.getPrice()).isEqualTo(350.00);
        assertThat(updatedProduct.getName()).isEqualTo("New Monitor");
        Optional<Product> foundProduct = productService.getProductById(updatedProduct.getId()); // Verifies persistence.
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getPrice()).isEqualTo(350.00);
    }

    /**
     * Tests deleting a product from the database.
     */
    @Test
    void testDeleteProduct() {
        Product product = productService.createProduct(new Product(null, "Webcam", 50.00));
        Long productId = product.getId();

        productService.deleteProduct(productId); // Deletes via service.

        Optional<Product> foundProduct = productService.getProductById(productId);
        assertThat(foundProduct).isNotPresent(); // Confirms deletion.
    }

    /**
     * Dynamically configures Spring Boot's DataSource properties to connect to the Testcontainers PostgreSQL instance.
     */
    static class PostgresContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgresContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresContainer.getUsername(),
                    "spring.datasource.password=" + postgresContainer.getPassword(),
                    "spring.datasource.driver-class-name=" + postgresContainer.getDriverClassName(),
                    "spring.jpa.hibernate.ddl-auto=create-drop" // Ensures schema is created/dropped for each test run.
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}