package com.jeju.nanaland.domain.search.service;

import static com.jeju.nanaland.domain.common.data.Category.EXPERIENCE;
import static com.jeju.nanaland.domain.common.data.Category.FESTIVAL;
import static com.jeju.nanaland.domain.common.data.Category.MARKET;
import static com.jeju.nanaland.domain.common.data.Category.NANA;
import static com.jeju.nanaland.domain.common.data.Category.NATURE;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.CompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnail;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailPost;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.search.dto.SearchResponse;
import com.jeju.nanaland.domain.search.dto.SearchResponse.SearchVolumeDto;
import com.jeju.nanaland.domain.search.dto.SearchResponse.ThumbnailDto;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
  private final FavoriteService favoriteService;
  private final RedisTemplate<String, String> redisTemplate;

  public SearchResponse.AllCategoryDto searchAllResultDto(MemberInfoDto memberInfoDto,
      String keyword) {

    Language locale = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();

    // Redis에 해당 검색어 count + 1
    updateSearchCountV1(keyword, locale);

    // offset: 0, pageSize: 2
    int page = 0;
    int size = 2;
    return SearchResponse.AllCategoryDto.builder()
        .nature(searchNatureResultDto(memberInfoDto, keyword, page, size))
        .festival(searchFestivalResultDto(memberInfoDto, keyword, page, size))
        .market(searchMarketResultDto(memberInfoDto, keyword, page, size))
        .experience(searchExperienceResultDto(memberInfoDto, keyword, page, size))
        .nana(searchNanaResultDto(memberInfoDto, keyword, page, size))
        .build();
  }

  public SearchResponse.ResultDto searchNatureResultDto(
      MemberInfoDto memberInfoDto,
      String keyword,
      int page,
      int size) {

    Language locale = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);
    Page<NatureCompositeDto> resultPage = natureRepository.searchCompositeDtoByKeyword(
        keyword, locale, pageable);

    List<Long> favoriteIds = favoriteService.getFavoritePostIdsWithMember(member);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (NatureCompositeDto dto : resultPage) {

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

  public SearchResponse.ResultDto searchFestivalResultDto(
      MemberInfoDto memberInfoDto,
      String keyword,
      int page,
      int size) {

    Language locale = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);
    Page<FestivalCompositeDto> resultPage = festivalRepository.searchCompositeDtoByKeyword(
        keyword, locale, pageable);

    List<Long> favoriteIds = favoriteService.getFavoritePostIdsWithMember(member);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (FestivalCompositeDto dto : resultPage) {

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

  public SearchResponse.ResultDto searchExperienceResultDto(
      MemberInfoDto memberInfoDto,
      String keyword,
      int page,
      int size) {

    Language locale = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);
    Page<ExperienceCompositeDto> resultPage = experienceRepository.searchCompositeDtoByKeyword(
        keyword, locale, pageable);

    List<Long> favoriteIds = favoriteService.getFavoritePostIdsWithMember(member);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (ExperienceCompositeDto dto : resultPage) {

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

  public SearchResponse.ResultDto searchMarketResultDto(
      MemberInfoDto memberInfoDto,
      String keyword,
      int page,
      int size) {

    Language locale = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);
    Page<MarketCompositeDto> resultPage = marketRepository.searchCompositeDtoByKeyword(
        keyword, locale, pageable);

    List<Long> favoriteIds = favoriteService.getFavoritePostIdsWithMember(member);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (MarketCompositeDto dto : resultPage) {
      // TODO: 이미지 추가 필요
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

  public SearchResponse.ResultDto searchNanaResultDto(
      MemberInfoDto memberInfoDto,
      String keyword,
      int page,
      int size) {

    Language locale = memberInfoDto.getLanguage();
    Member member = memberInfoDto.getMember();
    Pageable pageable = PageRequest.of(page, size);
    Page<NanaThumbnail> resultPage = nanaRepository.searchNanaThumbnailDtoByKeyword(
        keyword, locale, pageable);

    List<Long> favoriteIds = favoriteService.getFavoritePostIdsWithMember(member);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (NanaThumbnail thumbnail : resultPage) {
      thumbnails.add(
          ThumbnailDto.builder()
              .id(thumbnail.getId())
              .category(NANA.name())
              .firstImage(thumbnail.getFirstImage())
              .title(thumbnail.getHeading())
              .isFavorite(favoriteIds.contains(thumbnail.getId()))
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(thumbnails)
        .build();
  }

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
                  favoriteService.isPostInFavorite(memberInfoDto.getMember(), categoryContent,
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
          CompositeDto natureCompositeDto = natureRepository.findCompositeDtoById(postId,
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
            favoriteService.isPostInFavorite(memberInfoDto.getMember(), categoryContent,
                compositeDto.getId()))
        .build();
  }

  private List<String> getTopSearchVolumeList() {
    ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
    Set<String> topSearchVolumes = zSetOperations.reverseRange(SEARCH_VOLUME_KEY, 0, 3);
    return topSearchVolumes != null ? new ArrayList<>(topSearchVolumes) : new ArrayList<>();
  }
}
