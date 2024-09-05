package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.global.exception.NotFoundException;

public interface PostService {

  /**
   * Post 객체 조회
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @return Post
   * @throws NotFoundException (게시물 id, 카테고리)를 가진 게시물이 존재하지 않는 경우
   */
  Post getPost(Long postId, Category category);

  /**
   * 카드 정보 조회 - (postId, category, imageFile, title)
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @param language 언어 정보
   * @return PostCardDto
   * @throws NotFoundException (게시물 id, 카테고리, 언어)에 해당하는 게시물 정보가 없는 경우
   */
  PostCardDto getPostCardDto(Long postId, Category category, Language language);
}
