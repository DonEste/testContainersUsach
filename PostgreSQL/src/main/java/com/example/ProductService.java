package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing product-related business logic.
 * Handles CRUD operations and applies business rules.
 */
@Service
public class ProductService {

    @Autowired // This annotation is redundant with @RequiredArgsConstructor on a final field.
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Creates a new product after applying business validations.
     *
     * @param product The product to create.
     * @return The created product.
     * @throws IllegalArgumentException if product price is negative.
     */
    public Product createProduct(Product product) {
        if (product.getPrice() < 0) {
            throw new IllegalArgumentException("Product price cannot be negative"); // Business validation.
        }
        return productRepository.save(product);
    }

    /**
     * Retrieves all products.
     *
     * @return A list of all products.
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id The ID of the product.
     * @return An Optional containing the product if found, or empty otherwise.
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Updates an existing product.
     *
     * @param id             The ID of the product to update.
     * @param updatedProduct The product data to apply.
     * @return The updated product.
     * @throws RuntimeException if the product is not found.
     */
    public Product updateProduct(Long id, Product updatedProduct) {
        Product savedProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id)); // Ensures product exists.

        savedProduct.setName(updatedProduct.getName()); // Updates relevant fields.
        savedProduct.setPrice(updatedProduct.getPrice());
        productRepository.save(savedProduct); // Persists changes.
        return savedProduct;
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id The ID of the product to delete.
     */
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}