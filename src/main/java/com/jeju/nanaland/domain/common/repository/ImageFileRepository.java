package com.jeju.nanaland.domain.common.repository;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageFileRepository extends JpaRepository<ImageFile, Long>,
    ImageFileRepositoryCustom {

}
