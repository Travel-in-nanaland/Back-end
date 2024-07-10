package com.jeju.nanaland.domain.hashtag.repository;

import com.jeju.nanaland.domain.hashtag.entity.Keyword;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

  boolean existsByContent(String content);

  Optional<Keyword> findByContent(String content);

}
