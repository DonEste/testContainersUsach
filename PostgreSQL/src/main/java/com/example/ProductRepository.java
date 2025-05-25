package com.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data Access Layer for Product entities.
 * Extends JpaRepository to provide standard CRUD operations for {@link Product} objects.
 */
@Repository // Marks this interface as a Spring Data JPA repository.
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Inherits methods like save(), findById(), findAll(), deleteById(), etc., from JpaRepository.
    // No custom methods are defined here, meaning standard CRUD is sufficient.
}