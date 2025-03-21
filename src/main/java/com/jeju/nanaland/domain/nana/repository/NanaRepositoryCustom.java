package com.jeju.nanaland.domain.nana.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailPost;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.PreviewDto;
import com.jeju.nanaland.domain.nana.dto.NanaSearchDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NanaRepositoryCustom {

  //메인 페이지에서 슬라이드되는 Nana's pick 찾기
  List<PreviewDto> findTop4PreviewDtoOrderByCreatedAt(Language locale);

  //나나 pick 썸네일 페이징 조회
  Page<PreviewDto> findAllPreviewDtoOrderByCreatedAt(Language locale, Pageable pageable);

  //나나 pick 추천 게시물 4개 modifiedAt으로 최신순 4개
  List<PreviewDto> findRecommendPreviewDto(Language locale);

  Page<PreviewDto> searchNanaThumbnailDtoByKeyword(String keyword, Language locale,
      Pageable pageable);

  NanaThumbnailPost findNanaThumbnailPostDto(Long id, Language locale);

  PostPreviewDto findPostPreviewDto(Long postId, Language language);

  Page<NanaSearchDto> findSearchDtoByKeywordsUnion(List<String> keywords, Language language,
      Pageable pageable);

  Page<NanaSearchDto> findSearchDtoByKeywordsIntersect(List<String> keywords,
      Language language, Pageable pageable);

  Optional<String> findKoreanAddress(Long postId, Long number);
}
