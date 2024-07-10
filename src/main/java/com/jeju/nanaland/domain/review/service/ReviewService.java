package com.jeju.nanaland.domain.review.service;

import static com.jeju.nanaland.global.exception.ErrorCode.CATEGORY_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.POST_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_INVALID_CATEGORY;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.review.dto.ReviewRequest.CreateReviewDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewListDto;
import com.jeju.nanaland.domain.review.entity.Review;
import com.jeju.nanaland.domain.review.entity.ReviewImageFile;
import com.jeju.nanaland.domain.review.entity.ReviewKeyword;
import com.jeju.nanaland.domain.review.entity.ReviewTypeKeyword;
import com.jeju.nanaland.domain.review.repository.ReviewImageFileRepository;
import com.jeju.nanaland.domain.review.repository.ReviewKeywordRepository;
import com.jeju.nanaland.domain.review.repository.ReviewRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final ExperienceRepository experienceRepository;
  private final ReviewKeywordRepository reviewKeywordRepository;
  private final ReviewImageFileRepository reviewImageFileRepository;
  private final ImageFileService imageFileService;

  public ReviewListDto getReviewList(MemberInfoDto memberInfoDto, Category category, Long id,
      int page,
      int size) {
    if (category != Category.EXPERIENCE) {
      throw new BadRequestException(REVIEW_INVALID_CATEGORY.getMessage());
    }

    Pageable pageable = PageRequest.of(page, size);
    Page<ReviewDetailDto> reviewListByPostId = reviewRepository.findReviewListByPostId(
        memberInfoDto, category, id, pageable);

    Double totalAvgRating = reviewRepository.findTotalRatingAvg(category, id);

    return ReviewListDto.builder()
        .totalElements(reviewListByPostId.getTotalElements())
        .totalAvgRating(totalAvgRating)
        .data(reviewListByPostId.getContent())
        .build();
  }

  @Transactional
  public void saveReview(MemberInfoDto memberInfoDto, Long id, Category category,
      CreateReviewDto createReviewDto,
      List<MultipartFile> imageList) {

    Post post = getPostById(id, category);

    // 리뷰 저장
    Review review = reviewRepository.save(Review.builder()
        .member(memberInfoDto.getMember())
        .category(category)
        .post(post)
        .content(createReviewDto.getContent())
        .rating(createReviewDto.getRating())
        .build());

    // reviewKeyword
    // 혹시나 keyword 값 잘못 보낼 경우. (List, Set의 크기가 같아야 통과)
    Set<String> reviewKeywordStringSet = new HashSet<>(createReviewDto.getReviewKeywords());
    if (reviewKeywordStringSet.size() != createReviewDto.getReviewKeywords().size()) {
      throw new BadRequestException("review Keyword 값이 중복되었습니다.");
    }

    reviewKeywordStringSet.forEach(keyword ->
        reviewKeywordRepository.save(ReviewKeyword.builder()
            .review(review)
            .reviewTypeKeyword(ReviewTypeKeyword.valueOf(keyword))
            .build())
    );

    // reviewImageFile
    imageList.forEach(image ->
        reviewImageFileRepository.save(ReviewImageFile.builder()
            .imageFile(imageFileService.uploadAndSaveImageFile(image, true))
            .review(review)
            .build())
    );
  }

  private Post getPostById(Long id, Category category) {
    switch (category) {
      case EXPERIENCE -> {
        return experienceRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND.getMessage()));
      }
      //TODO 맛집 개발 시 추가하기
      default -> throw new BadRequestException(CATEGORY_NOT_FOUND.getMessage());
    }
  }
}
