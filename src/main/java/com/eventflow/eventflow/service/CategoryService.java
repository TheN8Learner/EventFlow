package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.CategoryRequestDto;
import com.eventflow.eventflow.dto.CategoryResponseDto;
import com.eventflow.eventflow.model.Category;
import com.eventflow.eventflow.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public CategoryResponseDto createCategory(CategoryRequestDto requestDto) {
        Category category = new Category(
                requestDto.getName()
        );
        category.setEvents(new ArrayList<>());
        categoryRepository.save(category);
        return new CategoryResponseDto(
                category.getId(),
                category.getName());
    }
}
