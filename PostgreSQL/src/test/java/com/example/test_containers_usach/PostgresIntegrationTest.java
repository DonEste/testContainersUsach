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

@DataJpaTest(
        // Use includeFilters to get also ProductService
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {ProductService.class} // Include your ProductService
        ))
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostgresIntegrationTest.PostgresContainerInitializer.class)
public class PostgresIntegrationTest {

    // Define the PostgreSQL container
    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb").withUsername("testuser").withPassword("testpass");

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository; // Keep this for deleteAll and direct repo interaction if needed

    @BeforeEach
    void setUp() {
        productRepository.deleteAll(); // Clean data before each test
    }

    @Test
    void testSaveAndFindProduct() {
        Product product = new Product(null, "Laptop", 1200.00); // Ensure ID is null for creation
        Product savedProduct = productService.createProduct(product);

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Laptop");

        Optional<Product> foundProduct = productService.getProductById(savedProduct.getId());
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Laptop");
    }

    @Test
    void testFindAllProducts() {
        productService.createProduct(new Product(null, "Keyboard", 75.00));
        productService.createProduct(new Product(null, "Mouse", 25.00));

        List<Product> products = productService.getAllProducts();
        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName).containsExactlyInAnyOrder("Keyboard", "Mouse");
    }

    @Test
    void testUpdateProduct() {
        Product product = productService.createProduct(new Product(null, "Monitor", 300.00));
        Product updatedDetails = new Product(null, "New Monitor", 350.00); // New details, ID is ignored

        Product updatedProduct = productService.updateProduct(product.getId(), updatedDetails);

        assertThat(updatedProduct.getPrice()).isEqualTo(350.00);
        assertThat(updatedProduct.getName()).isEqualTo("New Monitor"); // Also check name update
        Optional<Product> foundProduct = productService.getProductById(updatedProduct.getId());
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getPrice()).isEqualTo(350.00);
    }

    @Test
    void testDeleteProduct() {
        Product product = productService.createProduct(new Product(null, "Webcam", 50.00));
        Long productId = product.getId();

        productService.deleteProduct(productId);

        Optional<Product> foundProduct = productService.getProductById(productId);
        assertThat(foundProduct).isNotPresent();
    }

    /**
     * This initializer dynamically sets Spring Boot properties to connect to the Testcontainers PostgreSQL instance.
     */
    static class PostgresContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.datasource.url=" + postgresContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresContainer.getUsername(),
                    "spring.datasource.password=" + postgresContainer.getPassword(),
                    "spring.datasource.driver-class-name=" + postgresContainer.getDriverClassName(),
                    "spring.jpa.hibernate.ddl-auto=create-drop").applyTo(applicationContext.getEnvironment());
        }
    }
}