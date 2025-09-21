package com.library.repository;

import com.library.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find a category by exact name
    Optional<Category> findByName(String name);

    // Check if a category exists with the given name
    boolean existsByName(String name);

    // Find root categories (categories with no parent)
    List<Category> findRootCategories();

    // Find subcategories by parent category ID
    List<Category> findByParentCategoryId(Long parentId);
}
