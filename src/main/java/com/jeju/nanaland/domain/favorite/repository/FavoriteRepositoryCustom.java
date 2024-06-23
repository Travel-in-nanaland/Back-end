package com.jeju.nanaland.domain.favorite.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.ThumbnailDto;
import com.jeju.nanaland.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteRepositoryCustom {

  Page<ThumbnailDto> findNatureThumbnails(Member member, Locale locale, Pageable pageable);

  ThumbnailDto findNatureThumbnailByPostId(Member member, Long postId, Locale locale);

  Page<ThumbnailDto> findExperienceThumbnails(Member member, Locale locale, Pageable pageable);

  ThumbnailDto findExperienceThumbnailByPostId(Member member, Long postId, Locale locale);

  Page<ThumbnailDto> findFestivalThumbnails(Member member, Locale locale, Pageable pageable);

  ThumbnailDto findFestivalThumbnailByPostId(Member member, Long postId, Locale locale);

  Page<ThumbnailDto> findMarketThumbnails(Member member, Locale locale, Pageable pageable);

  ThumbnailDto findMarketThumbnailByPostId(Member member, Long postId, Locale locale);

  Page<ThumbnailDto> findNanaThumbnails(Member member, Locale locale, Pageable pageable);

  ThumbnailDto findNanaThumbnailByPostId(Member member, Long postId, Locale locale);
}
