package com.example.test_containers_usach;

import com.example.Product;
import com.example.ProductRepository;
import com.example.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * This test class focuses on unit testing the ProductService in isolation
 * by mocking its dependency on ProductRepository.
 * It ensures no actual database connection attempts are made.
 */
@SpringBootTest
// Explicitly define the configuration classes for this test's application context.
// By doing this, Spring Boot will NOT scan your main application's @SpringBootApplication class,
// which helps in achieving tighter isolation.
@ContextConfiguration(classes = PostgresServiceMockTest.TestConfig.class)
// Disable all auto-configurations related to database connectivity and JPA.
// This prevents Spring from trying to set up a DataSource, EntityManagerFactory, etc.
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,})
class PostgresServiceMockTest {

    // @MockitoBean automatically creates a Mockito mock and registers it as a Spring bean.
    // This mock will be injected into `productService`.
    @MockitoBean
    private ProductRepository productRepository;

    // Autowire the ProductService. It will receive the mocked ProductRepository.
    @Autowired
    private ProductService productService;

    @BeforeEach
    void setUp() {
        // Reset the mock's interactions and stubbing before each test
        // to ensure test isolation and prevent test leakage.
        reset(productRepository);
    }

    @Test
    void testCreateProduct_success() {
        Product newProduct = new Product(null, "Mock Laptop", 1200.00);
        // Simulate the repository returning a saved product with an ID
        Product savedProduct = new Product(1L, "Mock Laptop", 1200.00);

        // Configure the mock behavior: when save is called, return the simulated saved product
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Product result = productService.createProduct(newProduct);

        // Verify that the save method was called exactly once with the new product
        verify(productRepository, times(1)).save(newProduct);
        // Ensure no other unexpected interactions with the mock
        verifyNoMoreInteractions(productRepository);

        // Assert the result from the service
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Mock Laptop");
        assertThat(result.getPrice()).isEqualTo(1200.00);
    }

    @Test
    void testCreateProduct_negativePriceThrowsException() {
        Product newProduct = new Product(null, "Invalid Product", -10.00);

        // Assert that calling createProduct with a negative price throws IllegalArgumentException
        assertThatThrownBy(() -> productService.createProduct(newProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product price cannot be negative");

        // Verify that the save method on the repository was NEVER called
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testGetAllProducts_returnsListOfProducts() {
        Product product1 = new Product(1L, "Mock Keyboard", 75.00);
        Product product2 = new Product(2L, "Mock Mouse", 25.00);
        List<Product> mockProducts = Arrays.asList(product1, product2);

        // Configure the mock to return a predefined list of products when findAll is called
        when(productRepository.findAll()).thenReturn(mockProducts);

        List<Product> result = productService.getAllProducts();

        // Verify that findAll was called exactly once
        verify(productRepository, times(1)).findAll();
        verifyNoMoreInteractions(productRepository);

        // Assert the returned list
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(product1, product2);
    }

    @Test
    void testGetProductById_found() {
        Long productId = 1L;
        Product mockProduct = new Product(productId, "Mock Monitor", 300.00);

        // Configure the mock to return an Optional containing the product
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        Optional<Product> result = productService.getProductById(productId);

        // Verify findById was called with the correct ID
        verify(productRepository, times(1)).findById(productId);
        verifyNoMoreInteractions(productRepository);

        // Assert the result is present and contains the correct product
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Mock Monitor");
    }

    @Test
    void testGetProductById_notFound() {
        Long productId = 99L;

        // Configure the mock to return an empty Optional
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(productId);

        // Verify findById was called
        verify(productRepository, times(1)).findById(productId);
        verifyNoMoreInteractions(productRepository);

        // Assert the result is empty
        assertThat(result).isNotPresent();
    }

    @Test
    void testUpdateProduct_success() {
        Long productId = 1L;
        Product existingProduct = new Product(productId, "Old Name", 100.00);
        Product updatedProductDetails = new Product(null, "New Name", 150.00); // ID is ignored in update details

        // Mock finding the existing product
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        // Mock saving the updated product. Use thenAnswer to return the argument passed to save,
        // simulating the repository returning the saved entity.
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product productToSave = invocation.getArgument(0);
            productToSave.setId(productId); // Ensure the ID is maintained after saving by the mock
            return productToSave;
        });

        Product result = productService.updateProduct(productId, updatedProductDetails);

        // Verify findById was called
        verify(productRepository, times(1)).findById(productId);
        // Verify save was called. You can capture arguments for more precise verification if needed.
        verify(productRepository, times(1)).save(any(Product.class));
        verifyNoMoreInteractions(productRepository);

        // Assert the result
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getPrice()).isEqualTo(150.00);
    }

    @Test
    void testUpdateProduct_notFoundThrowsException() {
        Long productId = 99L;
        Product updatedProductDetails = new Product(null, "Non-existent", 150.00);

        // Mock not finding the product
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Expect a RuntimeException when product is not found
        assertThatThrownBy(() -> productService.updateProduct(productId, updatedProductDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found with ID: " + productId);

        // Verify findById was called, but save was NOT called
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_success() {
        Long productId = 1L;

        // Configure the mock to do nothing when deleteById is called
        doNothing().when(productRepository).deleteById(productId);

        productService.deleteProduct(productId);

        // Verify that deleteById was called exactly once
        verify(productRepository, times(1)).deleteById(productId);
        verifyNoMoreInteractions(productRepository);
    }

    /**
     * This nested @Configuration class defines the minimal Spring application context
     * required for this test.
     * It ensures that only `ProductService` is loaded into the context.
     */
    @Configuration
    @Import({ProductService.class}) // Only import the ProductService bean.
    // Its dependencies (like ProductRepository) will be resolved
    // by @MockitoBean or other beans in this minimal context.
    static class TestConfig {
        // No additional @Bean definitions are needed here for this test's scope.
    }
}