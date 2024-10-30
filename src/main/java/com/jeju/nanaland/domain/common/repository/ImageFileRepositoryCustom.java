package com.jeju.nanaland.domain.common.repository;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import java.util.List;

public interface ImageFileRepositoryCustom {

  List<ImageFileDto> findPostImageFiles(Long postId);
}
