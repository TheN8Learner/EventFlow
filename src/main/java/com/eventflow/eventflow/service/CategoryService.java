package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.CategoryRequestDto;
import com.eventflow.eventflow.dto.CategoryResponseDto;
import com.eventflow.eventflow.util.InputSanitizer;
import com.eventflow.eventflow.model.Category;
import com.eventflow.eventflow.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import com.eventflow.eventflow.exceptions.ResourceNotFoundException;
import com.eventflow.eventflow.dto.CategoryResponseDto;

import java.util.ArrayList;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;


    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Page<CategoryResponseDto> getCategories(Pageable pageable) {
        Page<Category> categories = categoryRepository.findAll(pageable);

        return categories.map(
                (category) -> new CategoryResponseDto(
                        category.getId(),
                        category.getName()
                )
        );
    }

        @PreAuthorize("hasRole('ADMIN')")
        public CategoryResponseDto createCategory(CategoryRequestDto requestDto) {
        Category category = new Category(
            InputSanitizer.text(requestDto.getName())
        );
        category.setEvents(new ArrayList<>());
        categoryRepository.save(category);
        return new CategoryResponseDto(
            category.getId(),
            category.getName());
        }

        @PreAuthorize("hasRole('ADMIN')")
        public CategoryResponseDto updateCategory(Long id, CategoryRequestDto requestDto) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setName(InputSanitizer.text(requestDto.getName()));
        Category saved = categoryRepository.save(category);
        return new CategoryResponseDto(saved.getId(), saved.getName());
        }

        @PreAuthorize("hasRole('ADMIN')")
        public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        categoryRepository.delete(category);
        }
}
