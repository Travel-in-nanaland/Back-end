package com.jeju.nanaland.domain.review.service;

import static com.jeju.nanaland.global.exception.ErrorCode.CATEGORY_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.NOT_FOUND_EXCEPTION;
import static com.jeju.nanaland.global.exception.ErrorCode.POST_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_IMAGE_BAD_REQUEST;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_INVALID_CATEGORY;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_KEYWORD_DUPLICATION;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_NOT_FOUND;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.review.dto.ReviewRequest.CreateReviewDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MyReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewListDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.StatusDto;
import com.jeju.nanaland.domain.review.entity.Review;
import com.jeju.nanaland.domain.review.entity.ReviewHeart;
import com.jeju.nanaland.domain.review.entity.ReviewImageFile;
import com.jeju.nanaland.domain.review.entity.ReviewKeyword;
import com.jeju.nanaland.domain.review.entity.ReviewTypeKeyword;
import com.jeju.nanaland.domain.review.repository.ReviewHeartRepository;
import com.jeju.nanaland.domain.review.repository.ReviewImageFileRepository;
import com.jeju.nanaland.domain.review.repository.ReviewKeywordRepository;
import com.jeju.nanaland.domain.review.repository.ReviewRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
  private final ReviewHeartRepository reviewHeartRepository;

  public ReviewListDto getReviewList(MemberInfoDto memberInfoDto, Category category, Long id,
      int page, int size) {
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
    if (imageList != null && imageList.size() > 5) {
      throw new BadRequestException(REVIEW_IMAGE_BAD_REQUEST.getMessage());
    }

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
      throw new BadRequestException(REVIEW_KEYWORD_DUPLICATION.getMessage());
    }

    reviewKeywordStringSet.forEach(keyword ->
        reviewKeywordRepository.save(ReviewKeyword.builder()
            .review(review)
            .reviewTypeKeyword(ReviewTypeKeyword.valueOf(keyword))
            .build())
    );

    // reviewImageFile
    if (imageList != null) {
      imageList.forEach(image ->
          reviewImageFileRepository.save(ReviewImageFile.builder()
              .imageFile(imageFileService.uploadAndSaveImageFile(image, true))
              .review(review)
              .build())
      );
    }
  }

  public MyReviewDetailDto getMyReviewById(MemberInfoDto memberInfoDto, Long reviewId) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new NotFoundException(
        NOT_FOUND_EXCEPTION.getMessage()));
    List<ImageFileDto> reviewImageList = reviewImageFileRepository.findAllById(reviewId)
        .stream()
        .map(image -> new ImageFileDto(image.getImageFile().getOriginUrl(),
            image.getImageFile().getThumbnailUrl()))
        .toList();
    List<String> reviewKeywordStringList = reviewKeywordRepository.findAllById(reviewId)
        .stream()
        .map(
            keyword -> keyword.getReviewTypeKeyword().getValueByLocale(memberInfoDto.getLanguage()))
        .toList();
    Category category = review.getCategory();
    MyReviewDetailDto myReviewDetail;

    if (category.equals(Category.EXPERIENCE)) {
      myReviewDetail = reviewRepository.findExperienceMyReviewDetail(
          review.getId(), memberInfoDto);
    }
    // TODO 맛집 생기면 주석 지우기
//    else if (category.equals(Category.RESTAURANT)) {
//      myReviewDetail = reviewRepository.findRestaurantMyReviewDetail(
//          review.getId(), memberInfoDto);
//    }
    else {
      throw new RuntimeException(NOT_FOUND_EXCEPTION.getMessage());
    }
    myReviewDetail.setImages(reviewImageList);
    myReviewDetail.setReviewKeywords(reviewKeywordStringList);

    return myReviewDetail;


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

  @Transactional
  public StatusDto toggleReviewHeart(MemberInfoDto memberInfoDto, Long id) {

    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(REVIEW_NOT_FOUND.getMessage()));

    Optional<ReviewHeart> reviewHeartOptional = reviewHeartRepository.findByMemberAndReview(
        memberInfoDto.getMember(), review);

    // 리뷰가 존재한다면, 삭제 후 false 응답
    if (reviewHeartOptional.isPresent()) {
      ReviewHeart reviewHeart = reviewHeartOptional.get();
      reviewHeartRepository.delete(reviewHeart);

      return StatusDto.builder()
          .isReviewHeart(false)
          .build();
    }

    // reviewHeart 생성, true 응답
    ReviewHeart reviewHeart = ReviewHeart.builder()
        .review(review)
        .member(memberInfoDto.getMember())
        .build();

    reviewHeartRepository.save(reviewHeart);

    return StatusDto.builder()
        .isReviewHeart(true)
        .build();
  }
}
