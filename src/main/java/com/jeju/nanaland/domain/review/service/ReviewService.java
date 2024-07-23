package com.jeju.nanaland.domain.review.service;

import static com.jeju.nanaland.global.exception.ErrorCode.CATEGORY_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.POST_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_IMAGE_BAD_REQUEST;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_INVALID_CATEGORY;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_NOT_FOUND;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.domain.review.dto.ReviewRequest.CreateReviewDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewListDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewPreviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewPreviewDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewListDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.SearchPostForReviewDto;
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
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private static final String SEARCH_AUTO_COMPLETE_HASH_KEY = "REVIEW AUTO COMPLETE:";
  private final ReviewRepository reviewRepository;
  private final ExperienceRepository experienceRepository;
  private final ReviewKeywordRepository reviewKeywordRepository;
  private final ReviewImageFileRepository reviewImageFileRepository;
  private final ImageFileService imageFileService;
  private final ReviewHeartRepository reviewHeartRepository;
  private final MarketRepository marketRepository;
  private final RedisTemplate<String, Object> redisTemplate;
  private final MemberRepository memberRepository;
  private final RestaurantRepository restaurantRepository;

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
      throw new BadRequestException("review Keyword 값이 중복되었습니다.");
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

  public List<SearchPostForReviewDto> getAutoCompleteSearchResultForReview(
      MemberInfoDto memberInfoDto, String keyword) {
    HashOperations<String, String, SearchPostForReviewDto> hashOperations = redisTemplate.opsForHash();
    Map<String, SearchPostForReviewDto> redisMap = hashOperations.entries(
        SEARCH_AUTO_COMPLETE_HASH_KEY + memberInfoDto.getLanguage()
            .name()); // 여기 KEY를 나중에 language를 붙이면 될듯
    List<SearchPostForReviewDto> dtoList = new ArrayList<>();
    for (String key : redisMap.keySet()) {
      if (key.contains(keyword)) {
        dtoList.add(redisMap.get(key));
      }
    }
    // title 사전 순으로 정렬
    dtoList.sort(Comparator.comparing(SearchPostForReviewDto::getTitle));
    return dtoList;
  }

  // TODO : 언어별로 SEARCH_AUTO_COMPLETE_HASH_KEY
  @PostConstruct
  private void init() {
    if ("test".equals(System.getProperty("spring.profiles.active"))) {
      return;
    }
    HashOperations<String, String, SearchPostForReviewDto> hashOperations = redisTemplate.opsForHash();

    /**
     * 테스트 용
     */
//    hashOperations.put(SEARCH_AUTO_COMPLETE_HASH_KEY + Language.KOREAN, "가나박물관, tag값",
//        SearchPostForReviewDto.builder()
//            .title("가나박물관")
//            .id(1L)
//            .category("category1")
//            .address("address1")
//            .firstImage(new ImageFileDto("image1", "image2"))
//            .build());
//    hashOperations.put(SEARCH_AUTO_COMPLETE_HASH_KEY + "KOREAN", "다라박물관",
//        SearchPostForReviewDto.builder()
//            .title("다라박물관")
//            .id(2L)
//            .category("category2")
//            .address("address2")
//            .firstImage(new ImageFileDto("image3", "image4"))
//            .build());
//    hashOperations.put(SEARCH_AUTO_COMPLETE_HASH_KEY + "KOREAN", "마바박물관",
//        SearchPostForReviewDto.builder()
//            .title("마바박물관")
//            .id(3L)
//            .category("category3")
//            .address("address3")
//            .firstImage(new ImageFileDto("image5", "image6"))
//            .build());

    // ---------------------------------------

    for (Language language : Language.values()) {
      experienceRepository.findAllSearchPostForReviewDtoByLanguage(language)
          .forEach(dto -> hashOperations.put(SEARCH_AUTO_COMPLETE_HASH_KEY + language.name(),
              dto.getTitle(), dto));

      // TODO 맛집 추가시 완성하기
//      restaurantRepository.findAllSearchPostForReviewDtoByLanguage(language)
//          .forEach(dto -> hashOperations.put(SEARCH_AUTO_COMPLETE_HASH_KEY + language.name(),
//              dto.getTitle(), dto));

    }

    Long test = hashOperations.size(SEARCH_AUTO_COMPLETE_HASH_KEY + "KOREAN");
    System.out.println("test = " + test);
  }

  private Post getPostById(Long id, Category category) {
    switch (category) {
      case EXPERIENCE -> {
        return experienceRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND.getMessage()));
      }

      case RESTAURANT -> {
        return restaurantRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND.getMessage()));
      }

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

  public MemberReviewListDto getReviewListByMember(MemberInfoDto memberInfoDto, Long memberId,
      int page, int size) {
    Member member = memberInfoDto.getMember();
    Language language = member.getLanguage();

    boolean isMyReview;
    if (memberId != null) {
      isMyReview = member.getId().equals(memberId);
      if (!isMyReview) {
        member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
      }
    }
    Pageable pageable = PageRequest.of(page, size);
    Page<MemberReviewDetailDto> reviewListByMember = reviewRepository.findReviewListByMember(
        member, language, pageable);

    return MemberReviewListDto.builder()
        .totalElements(reviewListByMember.getTotalElements())
        .data(reviewListByMember.getContent())
        .build();
  }

  public MemberReviewPreviewDto getReviewPreviewByMember(MemberInfoDto memberInfoDto,
      Long memberId) {
    Member member = memberInfoDto.getMember();
    Language language = member.getLanguage();

    boolean isMyReview;
    if (memberId != null) {
      isMyReview = member.getId().equals(memberId);
      if (!isMyReview) {
        member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
      }
    }
    List<MemberReviewPreviewDetailDto> reviewListByMember = reviewRepository.findReviewPreviewByMember(
        member, language);

    List<MemberReviewPreviewDetailDto> selectedReviews = new ArrayList<>();
    int totalWeight = 0;
    final int MAX_WEIGHT = 12;

    // 리뷰 선택 로직
    for (MemberReviewPreviewDetailDto reviewDetail : reviewListByMember) {
      int weight = (reviewDetail.getImageFileDto() != null) ? 2 : 1;

      if (totalWeight + weight <= MAX_WEIGHT) {
        selectedReviews.add(reviewDetail);
        totalWeight += weight;
      } else {
        break;
      }
    }

    Long totalCount = reviewRepository.findTotalCountByMember(member);

    return MemberReviewPreviewDto.builder()
        .totalElements(totalCount)
        .data(selectedReviews)
        .build();
  }
}
