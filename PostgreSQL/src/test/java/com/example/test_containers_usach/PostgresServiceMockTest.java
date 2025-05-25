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
 * Unit tests for the {@link ProductService}, isolating it from the database
 * by mocking its {@link ProductRepository} dependency.
 */
@SpringBootTest
@ContextConfiguration(classes = PostgresServiceMockTest.TestConfig.class)
// Excludes auto-configurations related to database setup to prevent actual database connections.
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class, // Prevents DataSource bean creation.
        HibernateJpaAutoConfiguration.class, // Prevents Hibernate configuration.
        JpaRepositoriesAutoConfiguration.class, // Prevents Spring Data JPA repository setup.
})
class PostgresServiceMockTest {

    // Replaces the real ProductRepository bean with a Mockito mock.
    // This allows controlling repository behavior and verifying interactions.
    @MockitoBean
    private ProductRepository productRepository;

    // Autowires the ProductService, which will receive the mocked ProductRepository.
    @Autowired
    private ProductService productService;

    /**
     * Resets the mock before each test to ensure test isolation.
     */
    @BeforeEach
    void setUp() {
        reset(productRepository); // Clears any previous stubbings or interaction recordings.
    }

    /**
     * Tests successful product creation.
     */
    @Test
    void testCreateProduct_success() {
        Product newProduct = new Product(null, "Mock Laptop", 1200.00);
        Product savedProduct = new Product(1L, "Mock Laptop", 1200.00);

        // Stubs the save method to return a product with an ID.
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Product result = productService.createProduct(newProduct);

        // Verifies that the save method was called exactly once with the new product.
        verify(productRepository, times(1)).save(newProduct);
        // Ensures no other methods were called on the mock.
        verifyNoMoreInteractions(productRepository);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Mock Laptop");
        assertThat(result.getPrice()).isEqualTo(1200.00);
    }

    /**
     * Tests that creating a product with a negative price throws an IllegalArgumentException.
     */
    @Test
    void testCreateProduct_negativePriceThrowsException() {
        Product newProduct = new Product(null, "Invalid Product", -10.00);

        // Asserts that calling createProduct with a negative price throws the expected exception.
        assertThatThrownBy(() -> productService.createProduct(newProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product price cannot be negative");

        // Verifies that the save method was never called, as validation should prevent it.
        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * Tests retrieving all products.
     */
    @Test
    void testGetAllProducts_returnsListOfProducts() {
        Product product1 = new Product(1L, "Mock Keyboard", 75.00);
        Product product2 = new Product(2L, "Mock Mouse", 25.00);
        List<Product> mockProducts = Arrays.asList(product1, product2);

        // Stubs findAll to return a predefined list of products.
        when(productRepository.findAll()).thenReturn(mockProducts);

        List<Product> result = productService.getAllProducts();

        // Verifies that findAll was called once.
        verify(productRepository, times(1)).findAll();
        verifyNoMoreInteractions(productRepository);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(product1, product2);
    }

    /**
     * Tests retrieving a product by ID when it exists.
     */
    @Test
    void testGetProductById_found() {
        Long productId = 1L;
        Product mockProduct = new Product(productId, "Mock Monitor", 300.00);

        // Stubs findById to return an Optional containing the mock product.
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        Optional<Product> result = productService.getProductById(productId);

        // Verifies findById was called once.
        verify(productRepository, times(1)).findById(productId);
        verifyNoMoreInteractions(productRepository);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Mock Monitor");
    }

    /**
     * Tests retrieving a product by ID when it does not exist.
     */
    @Test
    void testGetProductById_notFound() {
        Long productId = 99L;

        // Stubs findById to return an empty Optional.
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(productId);

        // Verifies findById was called once.
        verify(productRepository, times(1)).findById(productId);
        verifyNoMoreInteractions(productRepository);

        assertThat(result).isNotPresent();
    }

    /**
     * Tests successful product update.
     */
    @Test
    void testUpdateProduct_success() {
        Long productId = 1L;
        Product existingProduct = new Product(productId, "Old Name", 100.00);
        Product updatedProductDetails = new Product(null, "New Name", 150.00);

        // Stubs findById to return the existing product.
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        // Stubs save to return the product passed to it, simulating successful update.
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product productToSave = invocation.getArgument(0);
            productToSave.setId(productId); // Ensures the ID is retained in the returned object.
            return productToSave;
        });

        Product result = productService.updateProduct(productId, updatedProductDetails);

        // Verifies findById and save were both called once.
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
        verifyNoMoreInteractions(productRepository);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getPrice()).isEqualTo(150.00);
    }

    /**
     * Tests that updating a non-existent product throws a RuntimeException.
     */
    @Test
    void testUpdateProduct_notFoundThrowsException() {
        Long productId = 99L;
        Product updatedProductDetails = new Product(null, "Non-existent", 150.00);

        // Stubs findById to return an empty Optional, simulating not found.
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Asserts that calling updateProduct for a non-existent ID throws the expected exception.
        assertThatThrownBy(() -> productService.updateProduct(productId, updatedProductDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found with ID: " + productId);

        // Verifies findById was called, but save was not.
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * Tests successful product deletion.
     */
    @Test
    void testDeleteProduct_success() {
        Long productId = 1L;

        // Stubs deleteById to do nothing when called.
        doNothing().when(productRepository).deleteById(productId);

        productService.deleteProduct(productId);

        // Verifies deleteById was called exactly once.
        verify(productRepository, times(1)).deleteById(productId);
        verifyNoMoreInteractions(productRepository);
    }

    /**
     * Minimal Spring context configuration for this unit test.
     * Only imports {@link ProductService} as {@link ProductRepository} is mocked.
     */
    @Configuration
    @Import({ProductService.class}) // Imports the service to be tested.
    static class TestConfig {
    }
}