package com.jeju.nanaland.domain.search.service;

import static com.jeju.nanaland.domain.common.data.Category.EXPERIENCE;
import static com.jeju.nanaland.domain.common.data.Category.FESTIVAL;
import static com.jeju.nanaland.domain.common.data.Category.MARKET;
import static com.jeju.nanaland.domain.common.data.Category.NANA;
import static com.jeju.nanaland.domain.common.data.Category.NATURE;
import static com.jeju.nanaland.domain.common.data.Category.RESTAURANT;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.CompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceSearchDto;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.festival.dto.FestivalSearchDto;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.dto.MarketSearchDto;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailPost;
import com.jeju.nanaland.domain.nana.dto.NanaSearchDto;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nature.dto.NatureSearchDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantSearchDto;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.domain.search.dto.SearchResponse;
import com.jeju.nanaland.domain.search.dto.SearchResponse.SearchVolumeDto;
import com.jeju.nanaland.domain.search.dto.SearchResponse.ThumbnailDto;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

  private static final String SEARCH_VOLUME_KEY = "searchVolume";
  private static final String SEARCH_VOLUME_REGEX = ":";
  private final NanaRepository nanaRepository;
  private final NatureRepository natureRepository;
  private final ExperienceRepository experienceRepository;
  private final MarketRepository marketRepository;
  private final FestivalRepository festivalRepository;
  private final RestaurantRepository restaurantRepository;
  private final MemberFavoriteService memberFavoriteService;
  private final RedisTemplate<String, String> redisTemplate;

  // 카테고리 검색
  public SearchResponse.AllCategoryDto searchAll(MemberInfoDto memberInfoDto, String keyword) {

    Language locale = memberInfoDto.getLanguage();

    // Redis에 해당 검색어 count + 1
    updateSearchCountV1(keyword, locale);

    // offset: 0, pageSize: 2
    int page = 0;
    int size = 2;

    // 각 카테고리로 검색쿼리 비동기 요청
    CompletableFuture<SearchResponse.ResultDto> natureFuture = CompletableFuture.supplyAsync(
        () -> searchNature(memberInfoDto, keyword, page, size, null));
    CompletableFuture<SearchResponse.ResultDto> festivalFuture = CompletableFuture.supplyAsync(
        () -> searchFestival(memberInfoDto, keyword, page, size, null, null, null));
    CompletableFuture<SearchResponse.ResultDto> marketFuture = CompletableFuture.supplyAsync(
        () -> searchMarket(memberInfoDto, keyword, null, page, size));
    CompletableFuture<SearchResponse.ResultDto> activityFuture = CompletableFuture.supplyAsync(
        () -> searchExperience(memberInfoDto, ExperienceType.ACTIVITY, keyword, null, null, page,
            size));
    CompletableFuture<SearchResponse.ResultDto> cultureAndArtsFuture = CompletableFuture.supplyAsync(
        () -> searchExperience(memberInfoDto, ExperienceType.CULTURE_AND_ARTS, keyword, null,
            null, page, size));
    CompletableFuture<SearchResponse.ResultDto> restaurantFuture = CompletableFuture.supplyAsync(
        () -> searchRestaurant(memberInfoDto, keyword, null, null, page, size));
    CompletableFuture<SearchResponse.ResultDto> nanaFuture = CompletableFuture.supplyAsync(
        () -> searchNana(memberInfoDto, keyword, page, size));

    // 모든 비동기 작업이 완료될 때까지 기다린 후 각 결과를 수집
    CompletableFuture.allOf(
        natureFuture,
        festivalFuture,
        marketFuture,
        activityFuture,
        cultureAndArtsFuture,
        restaurantFuture,
        nanaFuture
    ).join();

    // 모든 결과를 수집하여 AllCategoryDto를 생성하여 반환
    return SearchResponse.AllCategoryDto.builder()
        .nature(natureFuture.join())
        .festival(festivalFuture.join())
        .market(marketFuture.join())
        .activity(activityFuture.join())
        .cultureAndArts(cultureAndArtsFuture.join())
        .restaurant(restaurantFuture.join())
        .nana(nanaFuture.join())
        .build();
  }

  /**
   * 자연 검색 공백으로 구분된 키워드가 4개 이하라면 제목, 내용, 해시태그, 지역필터에 키워드가 하나라도 포함되면 조회 4개보다 많다면 모든 키워드가 모두 포함되어야 조회
   *
   * @param memberInfoDto 유저 정보
   * @param keyword       유저 검색어
   * @param page          페이지
   * @param size          페이지 크기
   * @param addressTags   지역필터 리스트
   * @return 자연 검색 결과
   */
  public SearchResponse.ResultDto searchNature(MemberInfoDto memberInfoDto, String keyword,
      int page, int size, List<AddressTag> addressTags) {

    Language language = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);

    // 사용자 검색어 정규화
    List<String> normalizedKeywords = normalizeKeyword(keyword);

    Page<NatureSearchDto> resultPage;
    // 공백으로 구분한 키워드가 4개 이하라면 Union 검색
    if (normalizedKeywords.size() <= 4) {
      // 검색어 조합
      List<String> combinedKeywords = combineUserKeywords(normalizedKeywords);
      resultPage = natureRepository.findSearchDtoByKeywordsUnion(combinedKeywords,
          addressTags, language, pageable);
    }
    // 4개보다 많다면 Intersect 검색
    else {
      resultPage = natureRepository.findSearchDtoByKeywordsIntersect(normalizedKeywords,
          addressTags, language, pageable);
    }

    List<Long> favoriteIds = memberFavoriteService.getFavoritePostIdsWithMember(member);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (NatureSearchDto dto : resultPage) {

      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .category(NATURE.name())
              .firstImage(dto.getFirstImage())
              .title(dto.getTitle())
              .isFavorite(favoriteIds.contains(dto.getId()))
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(thumbnails)
        .build();
  }

  /**
   * 축제 검색 공백으로 구분된 키워드가 4개 이하라면 제목, 내용, 해시태그, 지역필터에 키워드가 하나라도 포함되면 조회 4개보다 많다면 모든 키워드가 모두 포함되어야 조회
   *
   * @param memberInfoDto 유저 정보
   * @param keyword       유저 검색어
   * @param page          페이지
   * @param size          페이지 크기
   * @param addressTags   지역필터 리스트
   * @param startDate     시작날짜
   * @param endDate       종료날짜
   * @return 축제 검색 결과
   */
  public SearchResponse.ResultDto searchFestival(MemberInfoDto memberInfoDto, String keyword,
      int page, int size, List<AddressTag> addressTags, LocalDate startDate, LocalDate endDate) {

    Language language = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);

    // 사용자 검색어 정규화
    List<String> normalizedKeywords = normalizeKeyword(keyword);

    Page<FestivalSearchDto> resultPage;
    // 공백으로 구분한 키워드가 4개 이하라면 Union 검색
    if (normalizedKeywords.size() <= 4) {
      // 검색어 조합
      List<String> combinedKeywords = combineUserKeywords(normalizedKeywords);
      resultPage = festivalRepository.findSearchDtoByKeywordsUnion(combinedKeywords, addressTags,
          startDate, endDate, language, pageable);
    }
    // 4개보다 많다면 Intersect 검색
    else {
      resultPage = festivalRepository.findSearchDtoByKeywordsIntersect(normalizedKeywords,
          addressTags, startDate, endDate, language, pageable);
    }

    List<Long> favoriteIds = memberFavoriteService.getFavoritePostIdsWithMember(member);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (FestivalSearchDto dto : resultPage) {

      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .category(FESTIVAL.name())
              .firstImage(dto.getFirstImage())
              .title(dto.getTitle())
              .isFavorite(favoriteIds.contains(dto.getId()))
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(thumbnails)
        .build();
  }

  /**
   * 이색체험 검색 공백으로 구분된 키워드가 4개 이하라면 제목, 내용, 해시태그, 지역필터에 키워드가 하나라도 포함되면 조회 4개보다 많다면 모든 키워드가 모두 포함되어야
   * 조회
   *
   * @param memberInfoDto 유저 정보
   * @param keyword       유저 검색어
   * @param addressTags   지열필터 리스트
   * @param page          페이지
   * @param size          페이지 크기
   * @return 이색체험 검색 결과
   */
  public SearchResponse.ResultDto searchExperience(MemberInfoDto memberInfoDto,
      ExperienceType experienceType, String keyword,
      List<ExperienceTypeKeyword> experienceTypeKeywords,
      List<AddressTag> addressTags, int page, int size) {

    Language language = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);

    // 사용자 검색어 정규화
    List<String> normalizedKeywords = normalizeKeyword(keyword);

    Page<ExperienceSearchDto> resultPage;
    // 공백으로 구분한 키워드가 4개 이하라면 Union 검색
    if (normalizedKeywords.size() <= 4) {
      // 검색어 조합
      List<String> combinedKeywords = combineUserKeywords(normalizedKeywords);
      resultPage = experienceRepository.findSearchDtoByKeywordsUnion(experienceType,
          combinedKeywords, experienceTypeKeywords, addressTags, language, pageable);
    }
    // 4개보다 많다면 Intersect 검색
    else {
      resultPage = experienceRepository.findSearchDtoByKeywordsIntersect(experienceType,
          normalizedKeywords, experienceTypeKeywords, addressTags, language, pageable);
    }

    List<Long> favoriteIds = memberFavoriteService.getFavoritePostIdsWithMember(member);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (ExperienceSearchDto dto : resultPage) {

      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .category(experienceType.name())
              .firstImage(dto.getFirstImage())
              .title(dto.getTitle())
              .isFavorite(favoriteIds.contains(dto.getId()))
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(thumbnails)
        .build();
  }

  /**
   * 전통시장 검색 공백으로 구분된 키워드가 4개 이하라면 제목, 내용, 해시태그, 지역필터에 키워드가 하나라도 포함되면 조회 4개보다 많다면 모든 키워드가 모두 포함되어야
   * 조회
   *
   * @param memberInfoDto 유저 정보
   * @param keyword       유저 검색어
   * @param addressTags   지역필터 리스트
   * @param page          페이지
   * @param size          페이지 크기
   * @return 전통시장 검색 결과
   */
  public SearchResponse.ResultDto searchMarket(MemberInfoDto memberInfoDto, String keyword,
      List<AddressTag> addressTags, int page, int size) {
    Language language = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);

    // 사용자 검색어 정규화
    List<String> normalizedKeywords = normalizeKeyword(keyword);

    Page<MarketSearchDto> resultPage;
    // 공백으로 구분한 키워드가 4개 이하라면 Union 검색
    if (normalizedKeywords.size() <= 4) {
      // 검색어 조합
      List<String> combinedKeywords = combineUserKeywords(normalizedKeywords);
      resultPage = marketRepository.findSearchDtoByKeywordsUnion(combinedKeywords,
          addressTags, language, pageable);
    }
    // 4개보다 많다면 Intersect 검색
    else {
      resultPage = marketRepository.findSearchDtoByKeywordsIntersect(normalizedKeywords,
          addressTags, language, pageable);
    }

    List<Long> favoriteIds = memberFavoriteService.getFavoritePostIdsWithMember(member);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (MarketSearchDto dto : resultPage) {

      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .category(MARKET.name())
              .firstImage(dto.getFirstImage())
              .title(dto.getTitle())
              .isFavorite(favoriteIds.contains(dto.getId()))
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(thumbnails)
        .build();
  }

  /**
   * 맛집 검색 공백으로 구분된 키워드가 4개 이하라면 제목, 내용, 해시태그, 지역필터에 키워드가 하나라도 포함되면 조회 4개보다 많다면 모든 키워드가 모두 포함되어야 조회
   *
   * @param memberInfoDto          유저 정보
   * @param keyword                유저 검색어
   * @param restaurantTypeKeywords 맛집 분류 리스트
   * @param addressTags            지역필터 리스트
   * @param page                   페이지
   * @param size                   페이지 크기
   * @return 맛집 검색 결과
   */
  public SearchResponse.ResultDto searchRestaurant(MemberInfoDto memberInfoDto, String keyword,
      List<RestaurantTypeKeyword> restaurantTypeKeywords, List<AddressTag> addressTags,
      int page, int size) {
    Language language = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);

    // 사용자 검색어 정규화
    List<String> normalizedKeywords = normalizeKeyword(keyword);

    Page<RestaurantSearchDto> resultPage;
    // 공백으로 구분한 키워드가 4개 이하라면 Union 검색
    if (normalizedKeywords.size() <= 4) {
      // 검색어 조합
      List<String> combinedKeywords = combineUserKeywords(normalizedKeywords);
      resultPage = restaurantRepository.findSearchDtoByKeywordsUnion(combinedKeywords,
          restaurantTypeKeywords, addressTags, language, pageable);
    }
    // 4개보다 많다면 Intersect 검색
    else {
      resultPage = restaurantRepository.findSearchDtoByKeywordsIntersect(normalizedKeywords,
          restaurantTypeKeywords, addressTags, language, pageable);
    }

    List<Long> favoriteIds = memberFavoriteService.getFavoritePostIdsWithMember(member);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (RestaurantSearchDto dto : resultPage) {

      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .category(RESTAURANT.name())
              .firstImage(dto.getFirstImage())
              .title(dto.getTitle())
              .isFavorite(favoriteIds.contains(dto.getId()))
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(thumbnails)
        .build();
  }

  /**
   * 나나스픽 검색 공백으로 구분된 키워드가 4개 이하라면 제목, 내용, 해시태그, 지역필터에 키워드가 하나라도 포함되면 조회 4개보다 많다면 모든 키워드가 모두 포함되어야
   * 조회
   *
   * @param memberInfoDto 유저 정보
   * @param keyword       유저 검색어
   * @param page          페이지
   * @param size          페이지 크기
   * @return 나나스픽 검색 결과
   */
  public SearchResponse.ResultDto searchNana(MemberInfoDto memberInfoDto, String keyword,
      int page, int size) {

    Language language = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);

    // 사용자 검색어 정규화
    List<String> normalizedKeywords = normalizeKeyword(keyword);

    Page<NanaSearchDto> resultPage;
    // 공백으로 구분한 키워드가 4개 이하라면 Union 검색
    if (normalizedKeywords.size() <= 4) {
      // 검색어 조합
      List<String> combinedKeywords = combineUserKeywords(normalizedKeywords);
      resultPage = nanaRepository.findSearchDtoByKeywordsUnion(combinedKeywords,
          language, pageable);
    }
    // 4개보다 많다면 Intersect 검색
    else {
      resultPage = nanaRepository.findSearchDtoByKeywordsIntersect(normalizedKeywords,
          language, pageable);
    }

    List<Long> favoriteIds = memberFavoriteService.getFavoritePostIdsWithMember(member);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (NanaSearchDto dto : resultPage) {

      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .category(NANA.name())
              .firstImage(dto.getFirstImage())
              .title(dto.getTitle())
              .isFavorite(favoriteIds.contains(dto.getId()))
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(thumbnails)
        .build();
  }

  // 인기 검색어 조회
  public List<String> getPopularSearch(Language locale) {
    String language = locale.name();

    // version 1
    String key = "ranking_" + language;

    /*
    // version 2
    LocalDateTime current = LocalDateTime.now();
    String keyIdx = String.valueOf(current.getHour() % 3);
    String key = "ranking_" + language + "_" + keyIdx;
    log.info("keyIdx : {}", keyIdx);
    */

    // 가장 검색어가 많은 8개
    ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
    Set<String> popularKeywords = zSetOperations.reverseRange(key, 0, 7);

    return popularKeywords != null ? new ArrayList<>(popularKeywords) : null;
  }

  // version1 : 인기검색어 정보 삭제하지 않고 계속 누적됨
  private void updateSearchCountV1(String title, Language locale) {
    String language = locale.name();
    String key = "ranking_" + language;

    redisTemplate.opsForZSet().incrementScore(key, title, 1);
  }

  // version2 : 인기검색어 정보 1시간마다 갱신됨
  private void updateSearchCountV2(String title, Language locale) {
    String language = locale.name();
    String key0 = "ranking_" + language + "_0";
    String key1 = "ranking_" + language + "_1";
    String key2 = "ranking_" + language + "_2";

    LocalDateTime current = LocalDateTime.now();
    int currentHour = current.getHour();
    switch (currentHour % 3) {
      case 0 -> {
        // 0번, 1번 key에 갱신
        redisTemplate.opsForZSet().incrementScore(key0, title, 1);
        redisTemplate.opsForZSet().incrementScore(key1, title, 1);

        // 3번 key 삭제
        redisTemplate.delete(key2);
      }
      case 1 -> {
        // 1번, 2번 key에 갱신
        redisTemplate.opsForZSet().incrementScore(key1, title, 1);
        redisTemplate.opsForZSet().incrementScore(key2, title, 1);

        // 3번 key 삭제
        redisTemplate.delete(key0);
      }
      case 2 -> {
        // 2번, 0번 key에 갱신
        redisTemplate.opsForZSet().incrementScore(key2, title, 1);
        redisTemplate.opsForZSet().incrementScore(key0, title, 1);

        // 1번 key 삭제
        redisTemplate.delete(key1);
      }
    }
  }

  public void updateSearchVolumeV1(Category categoryContent, Long id) {
    String value = categoryContent + SEARCH_VOLUME_REGEX + id;
    redisTemplate.opsForZSet().incrementScore(SEARCH_VOLUME_KEY, value, 1);
  }

  // 검색량 UP 게시물 조회
  public List<SearchVolumeDto> getTopSearchVolumePosts(MemberInfoDto memberInfoDto) {
    List<String> topSearchVolumeList = getTopSearchVolumeList();

    List<SearchVolumeDto> searchVolumeDtoList = new ArrayList<>();
    for (String element : topSearchVolumeList) {
      String[] parts = element.split(SEARCH_VOLUME_REGEX);
      Category categoryContent = Category.valueOf(parts[0]);
      Long postId = Long.valueOf(parts[1]);

      switch (categoryContent) {
        case NANA -> {
          NanaThumbnailPost nanaThumbnailPostDto = nanaRepository.findNanaThumbnailPostDto(
              postId, memberInfoDto.getLanguage()
          );
          if (nanaThumbnailPostDto == null) {
            throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
          }
          searchVolumeDtoList.add(SearchVolumeDto.builder()
              .id(nanaThumbnailPostDto.getId())
              .title(nanaThumbnailPostDto.getHeading())
              .firstImage(nanaThumbnailPostDto.getFirstImage())
              .category(categoryContent.name())
              .isFavorite(
                  memberFavoriteService.isPostInFavorite(memberInfoDto.getMember(), categoryContent,
                      nanaThumbnailPostDto.getId()))
              .build());
        }
        case FESTIVAL -> {
          CompositeDto festivalCompositeDto = festivalRepository.findCompositeDtoById(
              postId, memberInfoDto.getLanguage());

          searchVolumeDtoList.add(
              getSearchVolumeDto(memberInfoDto, categoryContent.name(), festivalCompositeDto));
        }
        case NATURE -> {
          CompositeDto natureCompositeDto = natureRepository.findNatureCompositeDto(postId,
              memberInfoDto.getLanguage());

          searchVolumeDtoList.add(
              getSearchVolumeDto(memberInfoDto, categoryContent.name(), natureCompositeDto));
        }
        case MARKET -> {
          CompositeDto marketCompositeDto = marketRepository.findCompositeDtoById(postId,
              memberInfoDto.getLanguage());

          searchVolumeDtoList.add(
              getSearchVolumeDto(memberInfoDto, categoryContent.name(), marketCompositeDto));
        }
        case EXPERIENCE -> {
          ExperienceCompositeDto experienceCompositeDto = experienceRepository.findCompositeDtoById(
              postId, memberInfoDto.getLanguage());

          searchVolumeDtoList.add(
              getSearchVolumeDto(memberInfoDto, experienceCompositeDto.getExperienceType().name(),
                  experienceCompositeDto));
        }
        case RESTAURANT -> {
          CompositeDto restaurantCompositeDto = restaurantRepository.findCompositeDtoById(
              postId, memberInfoDto.getLanguage());

          searchVolumeDtoList.add(
              getSearchVolumeDto(memberInfoDto, categoryContent.name(), restaurantCompositeDto)
          );
        }
        default -> throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
      }
    }
    return searchVolumeDtoList;
  }

  private SearchVolumeDto getSearchVolumeDto(MemberInfoDto memberInfoDto,
      String categoryContent, CompositeDto compositeDto) {
    if (compositeDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    Category category;
    if (categoryContent.equals(ExperienceType.ACTIVITY.name()) || categoryContent.equals(
        ExperienceType.CULTURE_AND_ARTS.name())) {
      category = EXPERIENCE;
    } else {
      category = Category.valueOf(categoryContent);
    }

    return SearchVolumeDto.builder()
        .id(compositeDto.getId())
        .firstImage(compositeDto.getFirstImage())
        .title(compositeDto.getTitle())
        .category(categoryContent)
        .isFavorite(
            memberFavoriteService.isPostInFavorite(memberInfoDto.getMember(), category,
                compositeDto.getId()))
        .build();
  }

  private List<String> getTopSearchVolumeList() {
    ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
    Set<String> topSearchVolumes = zSetOperations.reverseRange(SEARCH_VOLUME_KEY, 0, 3);
    return topSearchVolumes != null ? new ArrayList<>(topSearchVolumes) : new ArrayList<>();
  }

  /**
   * 사용자 검색어 정규화 검색어를 공백으로 구분하고 '-', '_' 제거, 모든 문자를 소문자로 변환
   *
   * @param keyword 사용자 검색어
   * @return 공백으로 구분되고 정규화한 검색어 리스트
   */
  List<String> normalizeKeyword(String keyword) {
    return Arrays.stream(keyword.split("\\s+"))  // 공백기준 분할
        .map(splittedKeyword -> splittedKeyword
            .replace("-", "")  // 하이픈 제거
            .replace("_", "")  // 언더스코어 제거
            .toLowerCase()  // 소문자로
        )
        .toList();
  }


  /**
   * 검색으로 들어온 키워드 조합 예를 들어 [jeju city restaurant]가 인자로 들어오면 [jeju, city, restaurant, jejucity,
   * jejucityrestaurant, cityrestaurant]를 반환
   *
   * @param keywords 사용자의 검색어 리스트
   * @return 조합된 사용자의 검색어
   */
  private List<String> combineUserKeywords(List<String> keywords) {
    if (keywords.size() == 1) {
      return keywords;
    }

    List<String> combinedKeywords = new ArrayList<>(keywords);
    for (int i = 0; i < keywords.size() - 1; i++) {
      StringBuilder combinedKeyword = new StringBuilder();
      combinedKeyword.append(keywords.get(i));
      for (int j = i + 1; j < keywords.size(); j++) {
        combinedKeyword.append(keywords.get(j));
        combinedKeywords.add(combinedKeyword.toString());
      }
    }

    return combinedKeywords;
  }
}
