package com.example;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Product entity for persistence in a database.
 */
@Entity // Marks this class as a JPA entity, mapping it to a database table.
@Data // Lombok: Generates getters, setters, equals, hashCode, and toString methods.
@NoArgsConstructor // Lombok: Generates a no-argument constructor (required by JPA).
@AllArgsConstructor // Lombok: Generates a constructor with all fields.
public class Product {

    @Id // Marks 'id' as the primary key of the entity.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures auto-increment for the ID.
    private Long id;
    private String name;
    private double price;

    // Constructor for creating a Product without an ID (useful for new entities).
    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }
}