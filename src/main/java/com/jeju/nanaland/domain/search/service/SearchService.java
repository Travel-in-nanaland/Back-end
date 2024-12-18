package com.jeju.nanaland.domain.search.service;

import static com.jeju.nanaland.domain.common.data.Category.EXPERIENCE;
import static com.jeju.nanaland.domain.common.data.Category.FESTIVAL;
import static com.jeju.nanaland.domain.common.data.Category.MARKET;
import static com.jeju.nanaland.domain.common.data.Category.NANA;
import static com.jeju.nanaland.domain.common.data.Category.NATURE;
import static com.jeju.nanaland.domain.common.data.Category.RESTAURANT;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.CompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceSearchDto;
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
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.domain.search.dto.SearchResponse;
import com.jeju.nanaland.domain.search.dto.SearchResponse.SearchVolumeDto;
import com.jeju.nanaland.domain.search.dto.SearchResponse.ThumbnailDto;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
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
        () -> searchNature(memberInfoDto, keyword, page, size));
    CompletableFuture<SearchResponse.ResultDto> festivalFuture = CompletableFuture.supplyAsync(
        () -> searchFestival(memberInfoDto, keyword, page, size));
    CompletableFuture<SearchResponse.ResultDto> marketFuture = CompletableFuture.supplyAsync(
        () -> searchMarket(memberInfoDto, keyword, page, size));
    CompletableFuture<SearchResponse.ResultDto> experienceFuture = CompletableFuture.supplyAsync(
        () -> searchExperience(memberInfoDto, keyword, page, size));
    CompletableFuture<SearchResponse.ResultDto> restaurantFuture = CompletableFuture.supplyAsync(
        () -> searchRestaurant(memberInfoDto, keyword, page, size));
    CompletableFuture<SearchResponse.ResultDto> nanaFuture = CompletableFuture.supplyAsync(
        () -> searchNana(memberInfoDto, keyword, page, size));

    // 모든 비동기 작업이 완료될 때까지 기다린 후 각 결과를 수집
    CompletableFuture.allOf(
        natureFuture,
        festivalFuture,
        marketFuture,
        experienceFuture,
        restaurantFuture,
        nanaFuture
    ).join();

    // 모든 결과를 수집하여 AllCategoryDto를 생성하여 반환
    return SearchResponse.AllCategoryDto.builder()
        .nature(natureFuture.join())
        .festival(festivalFuture.join())
        .market(marketFuture.join())
        .experience(experienceFuture.join())
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
   * @return 자연 검색 결과
   */
  public SearchResponse.ResultDto searchNature(MemberInfoDto memberInfoDto, String keyword,
      int page, int size) {

    Language language = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);
    List<String> normalizedKeywords = Arrays.stream(keyword.split("\\s+"))  // 공백기준 분할
        .map(String::toLowerCase)  // 소문자로
        .toList();

    Page<NatureSearchDto> resultPage;
    // 공백으로 구분한 키워드가 4개 이하라면 Union 검색
    if (normalizedKeywords.size() <= 4) {
      resultPage = natureRepository.findSearchDtoByKeywordsUnion(normalizedKeywords,
          language, pageable);
    }
    // 4개보다 많다면 Intersect 검색
    else {
      resultPage = natureRepository.findSearchDtoByKeywordsIntersect(normalizedKeywords,
          language, pageable);
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
   * @return 축제 검색 결과
   */
  public SearchResponse.ResultDto searchFestival(MemberInfoDto memberInfoDto, String keyword,
      int page, int size) {

    Language language = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);
    List<String> normalizedKeywords = Arrays.stream(keyword.split("\\s+"))  // 공백기준 분할
        .map(String::toLowerCase)  // 소문자로
        .toList();

    Page<FestivalSearchDto> resultPage;
    // 공백으로 구분한 키워드가 4개 이하라면 Union 검색
    if (normalizedKeywords.size() <= 4) {
      resultPage = festivalRepository.findSearchDtoByKeywordsUnion(normalizedKeywords,
          language, pageable);
    }
    // 4개보다 많다면 Intersect 검색
    else {
      resultPage = festivalRepository.findSearchDtoByKeywordsIntersect(normalizedKeywords,
          language, pageable);
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
   * @param page          페이지
   * @param size          페이지 크기
   * @return 이색체험 검색 결과
   */
  public SearchResponse.ResultDto searchExperience(MemberInfoDto memberInfoDto, String keyword,
      int page, int size) {

    Language language = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);
    List<String> normalizedKeywords = Arrays.stream(keyword.split("\\s+"))  // 공백기준 분할
        .map(String::toLowerCase)  // 소문자로
        .toList();

    Page<ExperienceSearchDto> resultPage;
    // 공백으로 구분한 키워드가 4개 이하라면 Union 검색
    if (normalizedKeywords.size() <= 4) {
      resultPage = experienceRepository.findSearchDtoByKeywordsUnion(normalizedKeywords,
          language, pageable);
    }
    // 4개보다 많다면 Intersect 검색
    else {
      resultPage = experienceRepository.findSearchDtoByKeywordsIntersect(normalizedKeywords,
          language, pageable);
    }

    List<Long> favoriteIds = memberFavoriteService.getFavoritePostIdsWithMember(member);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (ExperienceSearchDto dto : resultPage) {

      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .category(EXPERIENCE.name())
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
   * @param page          페이지
   * @param size          페이지 크기
   * @return 전통시장 검색 결과
   */
  public SearchResponse.ResultDto searchMarket(MemberInfoDto memberInfoDto, String keyword,
      int page, int size) {
    Language language = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);
    List<String> normalizedKeywords = Arrays.stream(keyword.split("\\s+"))  // 공백기준 분할
        .map(String::toLowerCase)  // 소문자로
        .toList();

    Page<MarketSearchDto> resultPage;
    // 공백으로 구분한 키워드가 4개 이하라면 Union 검색
    if (normalizedKeywords.size() <= 4) {
      resultPage = marketRepository.findSearchDtoByKeywordsUnion(normalizedKeywords,
          language, pageable);
    }
    // 4개보다 많다면 Intersect 검색
    else {
      resultPage = marketRepository.findSearchDtoByKeywordsIntersect(normalizedKeywords,
          language, pageable);
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
   * @param memberInfoDto 유저 정보
   * @param keyword       유저 검색어
   * @param page          페이지
   * @param size          페이지 크기
   * @return 맛집 검색 결과
   */
  public SearchResponse.ResultDto searchRestaurant(MemberInfoDto memberInfoDto, String keyword,
      int page, int size) {
    Language language = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);
    List<String> normalizedKeywords = Arrays.stream(keyword.split("\\s+"))  // 공백기준 분할
        .map(String::toLowerCase)  // 소문자로
        .toList();

    Page<RestaurantSearchDto> resultPage;
    // 공백으로 구분한 키워드가 4개 이하라면 Union 검색
    if (normalizedKeywords.size() <= 4) {
      resultPage = restaurantRepository.findSearchDtoByKeywordsUnion(normalizedKeywords,
          language, pageable);
    }
    // 4개보다 많다면 Intersect 검색
    else {
      resultPage = restaurantRepository.findSearchDtoByKeywordsIntersect(normalizedKeywords,
          language, pageable);
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
    List<String> normalizedKeywords = Arrays.stream(keyword.split("\\s+"))  // 공백기준 분할
        .map(String::toLowerCase)  // 소문자로
        .toList();

    Page<NanaSearchDto> resultPage;
    // 공백으로 구분한 키워드가 4개 이하라면 Union 검색
    if (normalizedKeywords.size() <= 4) {
      resultPage = nanaRepository.findSearchDtoByKeywordsUnion(normalizedKeywords,
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
              getSearchVolumeDto(memberInfoDto, categoryContent, festivalCompositeDto));
        }
        case NATURE -> {
          CompositeDto natureCompositeDto = natureRepository.findNatureCompositeDto(postId,
              memberInfoDto.getLanguage());

          searchVolumeDtoList.add(
              getSearchVolumeDto(memberInfoDto, categoryContent, natureCompositeDto));
        }
        case MARKET -> {
          CompositeDto marketCompositeDto = marketRepository.findCompositeDtoById(postId,
              memberInfoDto.getLanguage());

          searchVolumeDtoList.add(
              getSearchVolumeDto(memberInfoDto, categoryContent, marketCompositeDto));
        }
        case EXPERIENCE -> {
          CompositeDto experienceCompositeDto = experienceRepository.findCompositeDtoById(
              postId, memberInfoDto.getLanguage());

          searchVolumeDtoList.add(
              getSearchVolumeDto(memberInfoDto, categoryContent, experienceCompositeDto));
        }
        default -> throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
      }
    }
    return searchVolumeDtoList;
  }

  private SearchVolumeDto getSearchVolumeDto(MemberInfoDto memberInfoDto,
      Category categoryContent, CompositeDto compositeDto) {
    if (compositeDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    return SearchVolumeDto.builder()
        .id(compositeDto.getId())
        .firstImage(compositeDto.getFirstImage())
        .title(compositeDto.getTitle())
        .category(categoryContent.name())
        .isFavorite(
            memberFavoriteService.isPostInFavorite(memberInfoDto.getMember(), categoryContent,
                compositeDto.getId()))
        .build();
  }

  private List<String> getTopSearchVolumeList() {
    ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
    Set<String> topSearchVolumes = zSetOperations.reverseRange(SEARCH_VOLUME_KEY, 0, 3);
    return topSearchVolumes != null ? new ArrayList<>(topSearchVolumes) : new ArrayList<>();
  }
}
