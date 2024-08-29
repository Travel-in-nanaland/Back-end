package com.jeju.nanaland.domain.favorite.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.ThumbnailDto;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.member.entity.Member;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteRepositoryCustom {

  Page<ThumbnailDto> findNatureThumbnails(Member member, Language language, Pageable pageable);

  ThumbnailDto findNatureThumbnailByPostId(Member member, Long postId, Language language);

  Page<ThumbnailDto> findExperienceThumbnails(Member member, Language language, Pageable pageable);

  ThumbnailDto findExperienceThumbnailByPostId(Member member, Long postId, Language language);

  Page<ThumbnailDto> findFestivalThumbnails(Member member, Language language, Pageable pageable);

  ThumbnailDto findFestivalThumbnailByPostId(Member member, Long postId, Language language);

  Page<ThumbnailDto> findMarketThumbnails(Member member, Language language, Pageable pageable);

  ThumbnailDto findMarketThumbnailByPostId(Member member, Long postId, Language language);

  Page<ThumbnailDto> findNanaThumbnails(Member member, Language language, Pageable pageable);

  ThumbnailDto findNanaThumbnailByPostId(Member member, Long postId, Language language);

  Page<ThumbnailDto> findRestaurantThumbnails(Member member, Language language, Pageable pageable);

  ThumbnailDto findRestaurantThumbnailByPostId(Member member, Long postId, Language language);

  List<Favorite> findAllFavoriteToSendNotification();
}
