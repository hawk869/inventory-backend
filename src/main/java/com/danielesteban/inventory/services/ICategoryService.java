package com.danielesteban.inventory.services;

import com.danielesteban.inventory.model.Category;
import com.danielesteban.inventory.response.CategoryResponseRest;
import org.springframework.http.ResponseEntity;

public interface ICategoryService {

    ResponseEntity<CategoryResponseRest> search();
    ResponseEntity<CategoryResponseRest> searchById(Long id);
    ResponseEntity<CategoryResponseRest> save(Category category);
    ResponseEntity<CategoryResponseRest> update(Category category, Long id);
    ResponseEntity<CategoryResponseRest> deleteById(Long id);

}
