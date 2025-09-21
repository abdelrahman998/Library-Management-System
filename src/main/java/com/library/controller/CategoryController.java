package com.library.controller;

import com.library.entity.Category;
import com.library.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // Get all categories
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get category by name
    @GetMapping("/name/{name}")
    public ResponseEntity<Category> getCategoryByName(@PathVariable String name) {
        return categoryService.getCategoryByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get root categories
    @GetMapping("/root")
    public List<Category> getRootCategories() {
        return categoryService.getRootCategories();
    }

    // Get subcategories
    @GetMapping("/{parentId}/subcategories")
    public List<Category> getSubcategories(@PathVariable Long parentId) {
        return categoryService.getSubcategories(parentId);
    }

    // Create a new category
    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }

    // Update an existing category
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody Category categoryDetails) {
        try {
            Category updatedCategory = categoryService.updateCategory(id, categoryDetails);
            return ResponseEntity.ok(updatedCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get category tree
    @GetMapping("/tree")
    public List<CategoryService.CategoryTree> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    // Get category path
    @GetMapping("/{id}/path")
    public List<Category> getCategoryPath(@PathVariable Long id) {
        return categoryService.getCategoryPath(id);
    }

    // Get category statistics
    @GetMapping("/statistics")
    public CategoryService.CategoryStats getCategoryStatistics() {
        return categoryService.getCategoryStatistics();
    }

    // Get leaf categories
    @GetMapping("/leaf")
    public List<Category> getLeafCategories() {
        return categoryService.getLeafCategories();
    }

    // Search categories by name
    @GetMapping("/search")
    public List<Category> searchCategories(@RequestParam String name) {
        return categoryService.searchCategoriesByName(name);
    }

    // Move category to new parent
    @PutMapping("/{id}/move")
    public ResponseEntity<Category> moveCategory(
            @PathVariable Long id,
            @RequestParam(required = false) Long newParentId) {
        try {
            Category movedCategory = categoryService.moveCategoryToParent(id, newParentId);
            return ResponseEntity.ok(movedCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
