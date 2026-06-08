package com.eventflow.eventflow.config;

import com.eventflow.eventflow.model.Category;
import com.eventflow.eventflow.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategorySeeder implements ApplicationRunner {

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Meetup",
            "Conference",
            "Mariage",
            "Concert",
            "Workshop",
            "Seminaire",
            "Festival",
            "Anniversaire",
            "Formation",
            "Networking"
    );

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String categoryName : DEFAULT_CATEGORIES) {
            if (!categoryRepository.existsByNameIgnoreCase(categoryName)) {
                categoryRepository.save(new Category(categoryName));
            }
        }
    }
}
