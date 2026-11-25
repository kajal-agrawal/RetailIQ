package com.retailiq.dao;

import java.util.List;

/**
 * GenericDAO<T> - Generic Data Access Object Interface.
 * 
 * Academic Principles Demonstrated (Module II):
 * - Defining Interfaces & Abstract Methods.
 * - Generics (<T>) for type safety and code reuse.
 * - Default Methods in Interfaces (Java 8+ feature).
 * - Static Methods and Constants in Interfaces.
 * 
 * @param <T> The entity model type (e.g. Product, User, Category).
 */
public interface GenericDAO<T> {

    // Constant in Interface
    int DEFAULT_QUERY_LIMIT = 50;

    // Abstract Methods (Must be implemented by concrete DAO classes)
    T findById(int id);
    List<T> findAll();
    int insert(T entity);
    boolean update(T entity);
    boolean delete(int id);

    /**
     * Default Method: Checks if an entity exists by ID without loading the entire entity.
     * Can be overridden by subclasses if an optimized SQL COUNT query is preferred.
     */
    default boolean exists(int id) {
        return findById(id) != null;
    }

    /**
     * Static Method: Factory/Helper method returning the default pagination limit.
     */
    static int getDefaultLimit() {
        return DEFAULT_QUERY_LIMIT;
    }
}
