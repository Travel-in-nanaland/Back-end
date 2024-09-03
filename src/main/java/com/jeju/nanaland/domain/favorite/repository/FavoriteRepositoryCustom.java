package com.jeju.nanaland.domain.favorite.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.Thumbnail;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.member.entity.Member;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteRepositoryCustom {

  Page<Thumbnail> findNatureThumbnails(Member member, Language language, Pageable pageable);

  Thumbnail findNatureThumbnailByPostId(Member member, Long postId, Language language);

  Page<Thumbnail> findExperienceThumbnails(Member member, Language language, Pageable pageable);

  Thumbnail findExperienceThumbnailByPostId(Member member, Long postId, Language language);

  Page<Thumbnail> findFestivalThumbnails(Member member, Language language, Pageable pageable);

  Thumbnail findFestivalThumbnailByPostId(Member member, Long postId, Language language);

  Page<Thumbnail> findMarketThumbnails(Member member, Language language, Pageable pageable);

  Thumbnail findMarketThumbnailByPostId(Member member, Long postId, Language language);

  Page<Thumbnail> findNanaThumbnails(Member member, Language language, Pageable pageable);

  Thumbnail findNanaThumbnailByPostId(Member member, Long postId, Language language);

  Page<Thumbnail> findRestaurantThumbnails(Member member, Language language, Pageable pageable);

  Thumbnail findRestaurantThumbnailByPostId(Member member, Long postId, Language language);

  List<Favorite> findAllFavoriteToSendNotification();
}
