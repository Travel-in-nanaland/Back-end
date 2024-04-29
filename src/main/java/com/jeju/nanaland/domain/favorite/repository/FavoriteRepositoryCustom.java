package com.jeju.nanaland.domain.favorite.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.ThumbnailDto;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteRepositoryCustom {

  Page<ThumbnailDto> findNatureThumbnails(Long memberId, Locale locale, Pageable pageable);

  ThumbnailDto findNatureThumbnailByPostId(Long postId, Locale locale);

  Page<ThumbnailDto> findExperienceThumbnails(Long memberId, Locale locale, Pageable pageable);

  ThumbnailDto findExperienceThumbnailByPostId(Long postId, Locale locale);

  Page<ThumbnailDto> findFestivalThumbnails(Long memberId, Locale locale, Pageable pageable);

  ThumbnailDto findFestivalThumbnailByPostId(Long postId, Locale locale);

  Page<ThumbnailDto> findMarketThumbnails(Long memberId, Locale locale, Pageable pageable);

  ThumbnailDto findMarketThumbnailByPostId(Long postId, Locale locale);

  Page<ThumbnailDto> findNanaThumbnails(Long memberId, Locale locale, Pageable pageable);

  ThumbnailDto findNanaThumbnailByPostId(Long postId, Locale locale);

  Page<Favorite> findAllCategoryFavorite(Member member, Pageable pageable);
}
