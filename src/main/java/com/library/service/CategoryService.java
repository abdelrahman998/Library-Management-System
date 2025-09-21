package com.library.service;

import com.library.entity.Category;
import com.library.repository.CategoryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private CategoryRepository categoryRepository;

    /**
     * Retrieve all categories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Find category by ID
     */
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Find category by name
     */
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    /**
     * Get all root categories (categories with no parent)
     */
    public List<Category> getRootCategories() {
        return categoryRepository.findRootCategories();
    }

    /**
     * Get subcategories of a parent category
     */
    public List<Category> getSubcategories(Long parentId) {
        return categoryRepository.findByParentCategoryId(parentId);
    }

    /**
     * Create a new category
     */
    public Category createCategory(Category category) {
        // Validate unique name constraint
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category name already exists: " + category.getName());
        }

        // Validate category data
        validateCategoryData(category);

        // Validate parent category if specified
        if (category.getParentCategory() != null) {
            validateParentCategory(category.getParentCategory().getId(), null);
        }

        return categoryRepository.save(category);
    }

    /**
     * Update an existing category
     */
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Validate updated data
        validateCategoryData(categoryDetails);

        // Check if name is being changed and if new name already exists
        if (!category.getName().equals(categoryDetails.getName()) &&
                categoryRepository.existsByName(categoryDetails.getName())) {
            throw new RuntimeException("Category name already exists: " + categoryDetails.getName());
        }

        // Validate parent category change
        if (categoryDetails.getParentCategory() != null) {
            validateParentCategory(categoryDetails.getParentCategory().getId(), id);
        }

        // Update fields
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setParentCategory(categoryDetails.getParentCategory());

        return categoryRepository.save(category);
    }

    /**
     * Delete a category
     */
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Check if category has subcategories
        if (!getSubcategories(id).isEmpty()) {
            throw new RuntimeException("Cannot delete category with subcategories. Remove subcategories first.");
        }

        // Check if category has books
        if (category.getBooks() != null && !category.getBooks().isEmpty()) {
            throw new RuntimeException("Cannot delete category with books. Remove books from category first.");
        }

        categoryRepository.delete(category);
    }

    /**
     * Get category hierarchy as a tree structure
     */
    public List<CategoryTree> getCategoryTree() {
        List<Category> rootCategories = getRootCategories();
        return rootCategories.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());
    }

    /**
     * Get category path from root to specified category
     */
    public List<Category> getCategoryPath(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        List<Category> path = new ArrayList<>();
        Category current = category;

        while (current != null) {
            path.add(0, current); // Add to beginning to maintain order
            current = current.getParentCategory();
        }

        return path;
    }

    /**
     * Get all descendants of a category
     */
    public List<Category> getAllDescendants(Long categoryId) {
        List<Category> descendants = new ArrayList<>();
        List<Category> directChildren = getSubcategories(categoryId);

        for (Category child : directChildren) {
            descendants.add(child);
            descendants.addAll(getAllDescendants(child.getId()));
        }

        return descendants;
    }

    /**
     * Get category depth level
     */
    public int getCategoryDepth(Long categoryId) {
        List<Category> path = getCategoryPath(categoryId);
        return path.size() - 1; // Root level is 0
    }

    /**
     * Move category to a different parent
     */
    public Category moveCategoryToParent(Long categoryId, Long newParentId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        if (newParentId != null) {
            validateParentCategory(newParentId, categoryId);
            Category newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new RuntimeException("Parent category not found with id: " + newParentId));
            category.setParentCategory(newParent);
        } else {
            category.setParentCategory(null); // Move to root level
        }

        return categoryRepository.save(category);
    }

    /**
     * Get categories by depth level
     */
    public List<Category> getCategoriesByDepth(int depth) {
        return getAllCategories().stream()
                .filter(category -> getCategoryDepth(category.getId()) == depth)
                .collect(Collectors.toList());
    }

    /**
     * Get leaf categories (categories with no children)
     */
    public List<Category> getLeafCategories() {
        return getAllCategories().stream()
                .filter(category -> getSubcategories(category.getId()).isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Get categories with books
     */
    public List<Category> getCategoriesWithBooks() {
        return getAllCategories().stream()
                .filter(category -> category.getBooks() != null && !category.getBooks().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Get categories without books
     */
    public List<Category> getCategoriesWithoutBooks() {
        return getAllCategories().stream()
                .filter(category -> category.getBooks() == null || category.getBooks().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Search categories by name (case-insensitive)
     */
    public List<Category> searchCategoriesByName(String name) {
        return getAllCategories().stream()
                .filter(category -> category.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Get category statistics
     */
    public CategoryStats getCategoryStatistics() {
        List<Category> allCategories = getAllCategories();

        long totalCategories = allCategories.size();
        long rootCategories = getRootCategories().size();
        long leafCategories = getLeafCategories().size();
        long categoriesWithBooks = getCategoriesWithBooks().size();

        int maxDepth = allCategories.stream()
                .mapToInt(category -> getCategoryDepth(category.getId()))
                .max()
                .orElse(0);

        double avgBooksPerCategory = allCategories.stream()
                .filter(category -> category.getBooks() != null)
                .mapToInt(category -> category.getBooks().size())
                .average()
                .orElse(0.0);

        return new CategoryStats(totalCategories, rootCategories, leafCategories,
                categoriesWithBooks, maxDepth, avgBooksPerCategory);
    }

    /**
     * Check if category name exists
     */
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    /**
     * Validate category data
     */
    private void validateCategoryData(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new RuntimeException("Category name is required");
        }

        // Check for reasonable name length
        if (category.getName().length() > 100) {
            throw new RuntimeException("Category name is too long (maximum 100 characters)");
        }

        // Check description length if provided
        if (category.getDescription() != null && category.getDescription().length() > 500) {
            throw new RuntimeException("Category description is too long (maximum 500 characters)");
        }
    }

    /**
     * Validate parent category assignment
     */
    private void validateParentCategory(Long parentId, Long currentCategoryId) {
        if (parentId == null) return;

        // Check if parent exists
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent category not found with id: " + parentId));

        // Prevent self-reference
        if (currentCategoryId != null && parentId.equals(currentCategoryId)) {
            throw new RuntimeException("Category cannot be its own parent");
        }

        // Prevent circular references
        if (currentCategoryId != null) {
            List<Category> parentPath = getCategoryPath(parentId);
            if (parentPath.stream().anyMatch(cat -> cat.getId().equals(currentCategoryId))) {
                throw new RuntimeException("Circular reference detected: category cannot be ancestor of its parent");
            }
        }

        // Check for maximum depth (optional business rule)
        int parentDepth = getCategoryDepth(parentId);
        if (parentDepth >= 4) { // Maximum 5 levels (0-4)
            throw new RuntimeException("Maximum category depth (5 levels) would be exceeded");
        }
    }

    /**
     * Build category tree recursively
     */
    private CategoryTree buildCategoryTree(Category category) {
        List<Category> children = getSubcategories(category.getId());
        List<CategoryTree> childTrees = children.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());

        return new CategoryTree(category, childTrees);
    }

    /**
     * Get categories sorted by book count
     */
    public List<Category> getCategoriesSortedByBookCount() {
        return getAllCategories().stream()
                .sorted((c1, c2) -> {
                    int count1 = c1.getBooks() != null ? c1.getBooks().size() : 0;
                    int count2 = c2.getBooks() != null ? c2.getBooks().size() : 0;
                    return Integer.compare(count2, count1); // Descending order
                })
                .collect(Collectors.toList());
    }

    /**
     * Reorganize categories by moving all children of a deleted category to its parent
     */
    public void reorganizeCategoriesOnDeletion(Long categoryId) {
        Category categoryToDelete = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        List<Category> children = getSubcategories(categoryId);
        Category parentCategory = categoryToDelete.getParentCategory();

        // Move all children to the parent of the category being deleted
        for (Category child : children) {
            child.setParentCategory(parentCategory);
            categoryRepository.save(child);
        }

        // Now safe to delete the category
        categoryRepository.delete(categoryToDelete);
    }

    /**
     * Inner class for category tree structure
     */
    public static class CategoryTree {
        private final Category category;
        private final List<CategoryTree> children;

        public CategoryTree(Category category, List<CategoryTree> children) {
            this.category = category;
            this.children = children;
        }

        public Category getCategory() { return category; }
        public List<CategoryTree> getChildren() { return children; }
    }

    /**
     * Inner class for category statistics
     */
    @Data
    public static class CategoryStats {
        private final Long totalCategories;
        private final Long rootCategories;
        private final Long leafCategories;
        private final Long categoriesWithBooks;
        private final Integer maxDepth;
        private final Double averageBooksPerCategory;

        public CategoryStats(Long totalCategories, Long rootCategories, Long leafCategories,
                             Long categoriesWithBooks, Integer maxDepth, Double averageBooksPerCategory) {
            this.totalCategories = totalCategories;
            this.rootCategories = rootCategories;
            this.leafCategories = leafCategories;
            this.categoriesWithBooks = categoriesWithBooks;
            this.maxDepth = maxDepth;
            this.averageBooksPerCategory = averageBooksPerCategory;
        }
    }
}