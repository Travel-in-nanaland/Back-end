package com.jeju.nanaland.domain.common.repository;

import com.jeju.nanaland.domain.common.entity.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  public Optional<Category> findByContent(String content);
}
