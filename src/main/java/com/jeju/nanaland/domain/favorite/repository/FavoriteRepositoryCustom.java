package com.jeju.nanaland.domain.favorite.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.ThumbnailDto;
import com.jeju.nanaland.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteRepositoryCustom {

  Page<ThumbnailDto> findNatureThumbnails(Member member, Language locale, Pageable pageable);

  ThumbnailDto findNatureThumbnailByPostId(Member member, Long postId, Language locale);

  Page<ThumbnailDto> findExperienceThumbnails(Member member, Language locale, Pageable pageable);

  ThumbnailDto findExperienceThumbnailByPostId(Member member, Long postId, Language locale);

  Page<ThumbnailDto> findFestivalThumbnails(Member member, Language locale, Pageable pageable);

  ThumbnailDto findFestivalThumbnailByPostId(Member member, Long postId, Language locale);

  Page<ThumbnailDto> findMarketThumbnails(Member member, Language locale, Pageable pageable);

  ThumbnailDto findMarketThumbnailByPostId(Member member, Long postId, Language locale);

  Page<ThumbnailDto> findNanaThumbnails(Member member, Language locale, Pageable pageable);

  ThumbnailDto findNanaThumbnailByPostId(Member member, Long postId, Language locale);
}
