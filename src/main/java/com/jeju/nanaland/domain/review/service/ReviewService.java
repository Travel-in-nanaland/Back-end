package com.jeju.nanaland.domain.review.service;

import static com.jeju.nanaland.global.exception.ErrorCode.CATEGORY_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.EDIT_REVIEW_IMAGE_INFO_BAD_REQUEST;
import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_REVIEW_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.NOT_FOUND_EXCEPTION;
import static com.jeju.nanaland.global.exception.ErrorCode.NOT_MY_REVIEW;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_IMAGE_IMAGE_INFO_NOT_MATCH;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_INVALID_CATEGORY;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_KEYWORD_DUPLICATION;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_SELF_LIKE_FORBIDDEN;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.domain.review.dto.ReviewRequest.CreateReviewDto;
import com.jeju.nanaland.domain.review.dto.ReviewRequest.EditReviewDto;
import com.jeju.nanaland.domain.review.dto.ReviewRequest.EditReviewDto.EditImageInfoDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewListDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewPreviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewPreviewDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MyReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MyReviewDetailDto.MyReviewImageDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewListDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewStatusDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.SearchPostForReviewDto;
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
import com.jeju.nanaland.global.file.data.FileCategory;
import com.jeju.nanaland.global.file.service.FileUploadService;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
  private final RedisTemplate<String, Object> redisTemplate;
  private final MemberRepository memberRepository;
  private final RestaurantRepository restaurantRepository;
  private final FileUploadService fileUploadService;
  @Value("${cloud.aws.s3.reviewDirectory}")
  private String reviewImageDirectoryPath;

  // 게시물 별 리뷰 리스트 조회
  public ReviewListDto getReviewList(MemberInfoDto memberInfoDto, Category category, Long id,
      int page, int size) {
    if (category != Category.EXPERIENCE && category != Category.RESTAURANT) {
      throw new BadRequestException(REVIEW_INVALID_CATEGORY.getMessage());
    }

    Pageable pageable = PageRequest.of(page, size);
    Page<ReviewDetailDto> reviewListByPostId = reviewRepository.findReviewListByPostId(
        memberInfoDto, category, id, pageable);

    // 게시물 전체 평균 점수
    Double totalAvgRating = reviewRepository.findTotalRatingAvg(category, id);

    return ReviewListDto.builder()
        .totalElements(reviewListByPostId.getTotalElements())
        .totalAvgRating(totalAvgRating)
        .data(reviewListByPostId.getContent())
        .build();
  }

  // 리뷰 생성
  @Transactional
  public void saveReview(MemberInfoDto memberInfoDto, Long id, Category category,
      CreateReviewDto createReviewDto) {

    validateReviewRequest(category, createReviewDto);
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
      throw new BadRequestException(REVIEW_KEYWORD_DUPLICATION.getMessage());
    }

    reviewKeywordStringSet.stream()
        .map(ReviewTypeKeyword::valueOf)
        .filter(keyword -> keyword != ReviewTypeKeyword.NONE)
        .forEach(keyword -> reviewKeywordRepository.save(ReviewKeyword.builder()
            .review(review)
            .reviewTypeKeyword(keyword)
            .build()));

    // reviewImageFile
    List<String> fileKeys = createReviewDto.getFileKeys();
    if (fileKeys != null && !fileKeys.isEmpty()) {
      List<ReviewImageFile> reviewImageFiles = fileKeys.stream()
          .map((fileKey -> {
            ImageFile imageFile = imageFileService.getAndSaveImageFile(fileKey);
            return ReviewImageFile.builder()
                .review(review)
                .imageFile(imageFile)
                .build();
          })).toList();

      reviewImageFileRepository.saveAll(reviewImageFiles);
    }
  }

  private void validateReviewRequest(Category category, CreateReviewDto createReviewDto) {

    if (category != Category.EXPERIENCE && category != Category.RESTAURANT) {
      throw new BadRequestException(REVIEW_INVALID_CATEGORY.getMessage());
    }
    fileUploadService.validateFileKeys(createReviewDto.getFileKeys(), FileCategory.REVIEW);
  }


  // 리뷰를 위한 게시글 검색 자동완성
  public List<SearchPostForReviewDto> getAutoCompleteSearchResultForReview(
      MemberInfoDto memberInfoDto, String keyword) {
    HashOperations<String, String, SearchPostForReviewDto> hashOperations = redisTemplate.opsForHash();
    Map<String, SearchPostForReviewDto> redisMap = hashOperations.entries(
        SEARCH_AUTO_COMPLETE_HASH_KEY + memberInfoDto.getLanguage()
            .name()); // 여기 KEY를 나중에 language를 붙이면 될듯

    // 태호 박물관 -> "태호", "박물관"
    List<String> splitKeywordList = Arrays.asList(keyword.split(" "));
    // 태호 박물관 -> 태호박물관
    String mergedKeyword = String.join("", splitKeywordList);

    // 레디스에서 기존 keyword, 공백 없앤 keyword, 공백으로 분리한 keywordList로 검색한 값 title기준으로 사전 정렬
    List<SearchPostForReviewDto> keywordSearch = searchByKeyword(
        redisMap, keyword);
    List<SearchPostForReviewDto> mergedKeywordSearch = searchByKeyword(redisMap, mergedKeyword);
    List<SearchPostForReviewDto> splitKeywordSearch = searchByKeywordList(
        redisMap, splitKeywordList);

    // keywordSearch에 존재하지 않을 경우에만 결과에 추가 (중복 제거)
    for (SearchPostForReviewDto dto : mergedKeywordSearch) {
      if (!keywordSearch.contains(dto)) {
        keywordSearch.add(dto);
      }
    }
    for (SearchPostForReviewDto dto : splitKeywordSearch) {
      if (!keywordSearch.contains(dto)) {
        keywordSearch.add(dto);
      }
    }
    return keywordSearch;
  }

  // 리뷰 좋아요 토글
  @Transactional
  public ReviewStatusDto toggleReviewHeart(MemberInfoDto memberInfoDto, Long id) {

    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(REVIEW_NOT_FOUND.getMessage()));

    if (review.getMember().equals(memberInfoDto.getMember())) {
      throw new BadRequestException(REVIEW_SELF_LIKE_FORBIDDEN.getMessage());
    }

    Optional<ReviewHeart> reviewHeartOptional = reviewHeartRepository.findByMemberAndReview(
        memberInfoDto.getMember(), review);

    // 리뷰가 존재한다면, 삭제 후 false 응답
    if (reviewHeartOptional.isPresent()) {
      ReviewHeart reviewHeart = reviewHeartOptional.get();
      reviewHeartRepository.delete(reviewHeart);

      return ReviewStatusDto.builder()
          .isReviewHeart(false)
          .build();
    }

    // reviewHeart 생성, true 응답
    ReviewHeart reviewHeart = ReviewHeart.builder()
        .review(review)
        .member(memberInfoDto.getMember())
        .build();

    reviewHeartRepository.save(reviewHeart);

    return ReviewStatusDto.builder()
        .isReviewHeart(true)
        .build();
  }

  // 내가 쓴 리뷰 상세 조회
  public MyReviewDetailDto getMyReviewDetail(MemberInfoDto memberInfoDto, Long reviewId) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new NotFoundException(
        NOT_FOUND_EXCEPTION.getMessage()));
    List<MyReviewImageDto> reviewImageList = reviewImageFileRepository.findAllByReview(review)
        .stream()
        .map(image -> MyReviewImageDto.builder()
            .id(image.getId()) // reviewImageFile id 전달
            .originUrl(image.getImageFile().getOriginUrl())
            .thumbnailUrl(image.getImageFile().getThumbnailUrl())
            .build())
        .toList();

    List<ReviewTypeKeyword> reviewKeywordStringList = reviewKeywordRepository.findAllByReview(
            review)
        .stream()
        .map(ReviewKeyword::getReviewTypeKeyword)
        .toList();
    Category category = review.getCategory();
    MyReviewDetailDto myReviewDetail;

    if (category.equals(Category.EXPERIENCE)) {
      myReviewDetail = reviewRepository.findExperienceMyReviewDetail(review.getId(), memberInfoDto);
    } else if (category.equals(Category.RESTAURANT)) {
      myReviewDetail = reviewRepository.findRestaurantMyReviewDetail(review.getId(), memberInfoDto);
    } else {
      throw new NotFoundException(NOT_FOUND_EXCEPTION.getMessage());
    }
    myReviewDetail.setImages(reviewImageList);
    myReviewDetail.setReviewKeywords(reviewKeywordStringList);

    return myReviewDetail;
  }

  // 내가 쓴 리뷰 삭제
  @Transactional
  public void deleteMyReview(MemberInfoDto memberInfoDto, Long reviewId) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new NotFoundException(
        REVIEW_NOT_FOUND.getMessage()));

    // 삭제하려는 리뷰가 본인의 리뷰인지 체크
    if (!review.getMember().equals(memberInfoDto.getMember())) {
      throw new BadRequestException(NOT_MY_REVIEW.getMessage());
    }

    // s3에서 삭제하기 위해 reviewImageFile의 imageFile 추출
    List<ImageFile> imageFileList = reviewImageFileRepository.findAllByReview(review)
        .stream()
        .map(ReviewImageFile::getImageFile)
        .toList();

    // reviewImageFile 삭제, cascade remove 설정으로 imageFile 함께 삭제
    reviewImageFileRepository.deleteAllByReview(review);

    // reviewHeart 삭제
    reviewHeartRepository.deleteAllByReview(review);

    // review 삭제, cascade remove 설정으로 reviewKeywords 같이 삭제
    reviewRepository.deleteById(reviewId);

    // s3에서도 이미지 삭제
    for (ImageFile imageFile : imageFileList) {
      imageFileService.deleteImageFileInS3ByImageFile(imageFile, reviewImageDirectoryPath);
    }
  }

  // 내가 쓴 리뷰 수정
  @Transactional
  public void updateMyReview(MemberInfoDto memberInfoDto, Long reviewId,
      EditReviewDto editReviewDto) {
    // 유저가 쓴 리뷰 조회
    Review review = reviewRepository.findReviewByIdAndMember(reviewId, memberInfoDto.getMember())
        .orElseThrow(() -> new NotFoundException(MEMBER_REVIEW_NOT_FOUND.getMessage()));

    // rating 업데이트 되었으면 수정
    if (review.getRating() != editReviewDto.getRating()) {
      review.updateRating(editReviewDto.getRating());
    }

    // content 업데이트 되었으면 수정
    if (!review.getContent().equals(editReviewDto.getContent())) {
      review.updateContent(editReviewDto.getContent());
    }

    // reviewKeyword 수정
    updateReviewKeyword(review, editReviewDto);
    // 이미지가 있는 리뷰라면 수정
    updateReviewImages(review, editReviewDto, editReviewDto.getFileKeys());
  }

  // 회원 별 리뷰 리스트 조회
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
        memberInfoDto.getMember().getId(), member, language, pageable);

    return MemberReviewListDto.builder()
        .totalElements(reviewListByMember.getTotalElements())
        .data(reviewListByMember.getContent())
        .build();
  }

  // 회원 별 리뷰 썸네일 리스트 조회
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
        memberInfoDto.getMember().getId(), member, language);

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

    Long totalCount = reviewRepository.findTotalCountByMember(memberInfoDto.getMember().getId(),
        member);

    return MemberReviewPreviewDto.builder()
        .totalElements(totalCount)
        .data(selectedReviews)
        .build();
  }

  private Post getPostById(Long id, Category category) {
    switch (category) {
      case EXPERIENCE -> {
        return experienceRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(NOT_FOUND_EXCEPTION.getMessage()));
      }

      case RESTAURANT -> {
        return restaurantRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(NOT_FOUND_EXCEPTION.getMessage()));
      }

      default -> throw new BadRequestException(CATEGORY_NOT_FOUND.getMessage());
    }
  }

  private void updateReviewKeyword(Review review, EditReviewDto editReviewDto) {
    List<String> editReviewKeywords = editReviewDto.getReviewKeywords();
    List<ReviewKeyword> originReviewKeywords = review.getReviewKeywords().stream().toList();
    List<String> stringOriginReviewKeywords = originReviewKeywords
        .stream()
        .map(keyword -> keyword.getReviewTypeKeyword().name())
        .toList();

    // 기존 값 모두 false, 기존 키워드들이 수정되어 제출된 리뷰에도 있는지 판단
    // 기존에있고 수정되어 제출된 리뷰에도 있다면 true, 기존에 있지만 수정되어 제출된 리뷰에 없다면 false로 체크 예정
    boolean[] existKeyword = new boolean[stringOriginReviewKeywords.size()];

    // 수정되어 제출된 keyword가 기존 keywrod에 있는 것인지 확인.
    for (String editKeyword : editReviewKeywords) {
      boolean isNew = true;
      for (int j = 0; j < stringOriginReviewKeywords.size(); j++) {
        // 기존에 있던 키워드면 existKeyword = True, isNew = false
        if (editKeyword.equals(stringOriginReviewKeywords.get(j))) {
          existKeyword[j] = true;
          isNew = false;
          break;
        }
      }
      // j로 돌아가는 반복문에서 isNew가 false로 바뀌지 않았다면 새로 추가된 키워드이므로 저장
      if (isNew) {
        reviewKeywordRepository.save(ReviewKeyword.builder()
            .review(review)
            .reviewTypeKeyword(ReviewTypeKeyword.valueOf(editKeyword))
            .build());
      }
    }
    // existKeyword가 false라는 것은 기존에는 있었지만 리뷰를 수정하면서 삭제된 것이므로 제거
    for (int i = 0; i < stringOriginReviewKeywords.size(); i++) {
      if (!existKeyword[i]) {
        reviewKeywordRepository.deleteById(originReviewKeywords.get(i).getId());
      }
    }
  }

  private void updateReviewImages(Review review, EditReviewDto editReviewDto,
      List<String> fileKeys) {

    List<EditImageInfoDto> editImageInfoList = editReviewDto.getEditImageInfoList();
    List<ReviewImageFile> originReviewImageList = reviewImageFileRepository.findAllByReview(review);

    // 바뀐 이미지 몇 개인지
    int totalNewImage = (int) editImageInfoList.stream()
        .filter(EditImageInfoDto::isNewImage)
        .count();

    // 수정된 리뷰에 이미지가 있을 경우
    // fileKeys의 크기와 editImageInfo의 newImage가 true인 것의 수가 같은지 비교
    if ((fileKeys != null) && (totalNewImage != fileKeys.size())) {
      throw new BadRequestException(REVIEW_IMAGE_IMAGE_INFO_NOT_MATCH.getMessage());
    }

    // 기존 ReviewImageFile들의 id를 저장
    // 나중에 여기에 남아있는 이미지는 삭제되었다고 판단할 예정
    Set<Long> existImageIds = originReviewImageList
        .stream()
        .map(ReviewImageFile::getId)
        .collect(Collectors.toSet());

    List<ReviewImageFile> reviewImageFiles = new ArrayList<>();
    int newImageIdx = 0;
    for (EditImageInfoDto editImageInfo : editImageInfoList) {
      // 수정 제출된 이미지가
      if (editImageInfo.isNewImage()) { // 새로 제출된 이미지라면 저장
        assert fileKeys != null;
        ImageFile imageFile = imageFileService.getAndSaveImageFile(fileKeys.get(newImageIdx++));
        ReviewImageFile reviewImageFile = ReviewImageFile.builder()
            .imageFile(imageFile)
            .review(review)
            .build();
        reviewImageFiles.add(reviewImageFile);
      } else { // 원래 있던 이미지라면
        if (!existImageIds.remove(
            editImageInfo.getId())) { // set에서 제거하기 , 제거가 안되었다면 imageInfo 잘못 준것 / 나중에 여기 남아있는 건 삭제해야한다고 판단.
          throw new BadRequestException(EDIT_REVIEW_IMAGE_INFO_BAD_REQUEST.getMessage());
        }
      }
    }
    reviewImageFileRepository.saveAll(reviewImageFiles);

    // 삭제되어야할 reviewImageFile
    List<ReviewImageFile> allById = reviewImageFileRepository.findAllById(existImageIds);
    List<ImageFile> deleteImageFiles = allById.stream()
        .filter(reviewImageFile -> existImageIds.contains(reviewImageFile.getId()))
        .map(ReviewImageFile::getImageFile).toList();

    // reviewImageFile 삭제 -> cascade remove로 imageFile도 삭제됨
    reviewImageFileRepository.deleteAll(allById);

    // s3에서 삭제
    deleteImageFiles.forEach(
        deleteImageFile -> imageFileService.deleteImageFileInS3ByImageFile(deleteImageFile,
            reviewImageDirectoryPath));

  }

  @PostConstruct
  private void initAutoCompleteData() {
    // 테스트 돌릴 때 redis가 켜지지 않아 이 부분 에러남. 테스트 일때는 실행되지 않도록 설정
    if ("test".equals(System.getProperty("spring.profiles.active"))) {
      return;
    }

    // 서버 재시동 시 기존 hash 삭제
    for (Language language : Language.values()) {
      Boolean delete = redisTemplate.delete(SEARCH_AUTO_COMPLETE_HASH_KEY + language.name());
    }

    HashOperations<String, String, SearchPostForReviewDto> hashOperations = redisTemplate.opsForHash();

    for (Language language : Language.values()) {
      // ACTIVITY 저장
      experienceRepository.findAllSearchActivityPostForReviewDtoByLanguage(language)
          .forEach(dto -> {
            hashOperations.put(SEARCH_AUTO_COMPLETE_HASH_KEY + language.name(),
                dto.getTitle(), dto);
          });

      // CultureAndArts 저장
      experienceRepository.findAllSearchCultureAndArtsPostForReviewDtoByLanguage(language)
          .forEach(dto -> {
            hashOperations.put(SEARCH_AUTO_COMPLETE_HASH_KEY + language.name(),
                dto.getTitle(), dto);
          });

      // restaurant 저장
      restaurantRepository.findAllSearchPostForReviewDtoByLanguage(language)
          .forEach(dto -> {
            hashOperations.put(SEARCH_AUTO_COMPLETE_HASH_KEY + language.name(),
                dto.getTitle(), dto);
          });

    }

  }

  /**
   * redis에서 모든 값 가져온 후 key가 keyword를 포함한 것 찾기 찾은 것들에 value->(SearchPostForReviewDto) 가져오기 title로
   * 정렬
   */
  private List<SearchPostForReviewDto> searchByKeyword(Map<String, SearchPostForReviewDto> redisMap,
      String keyword) {
    return redisMap.entrySet().stream()
        .filter(entry -> entry.getKey().toLowerCase().contains(keyword.toLowerCase()))
        .map(Map.Entry::getValue)
        .sorted(Comparator.comparing(SearchPostForReviewDto::getTitle))
        .collect(Collectors.toList());
  }

  /**
   * redis에서 모든 값 가져온 후 List<> keyword를 stream돌려서 key가 keyword를 포함한 것 찾기 찾은 것들에
   * value->(SearchPostForReviewDto) 가져오기 title로 정렬
   */
  private List<SearchPostForReviewDto> searchByKeywordList(
      Map<String, SearchPostForReviewDto> redisMap, List<String> keywords) {
    return redisMap.entrySet().stream()
        .filter(entry -> keywords.stream()
            .anyMatch(keyword -> entry.getKey().toLowerCase().contains(keyword.toLowerCase())))
        .map(Map.Entry::getValue)
        .sorted(Comparator.comparing(SearchPostForReviewDto::getTitle))
        .collect(Collectors.toList());
  }

}
