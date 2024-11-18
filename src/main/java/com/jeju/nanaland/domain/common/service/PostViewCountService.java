package com.jeju.nanaland.domain.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PopularPostPreviewDto;
import com.jeju.nanaland.domain.common.repository.PostRepository;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.global.util.RedisUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostViewCountService {

  private static final String POPULAR_POSTS = "POPULAR POSTS:";
  private static final int MAX_POPULAR_POSTS = 3;
  private final PostRepository postRepository;
  private final ExperienceRepository experienceRepository;
  private final FestivalRepository festivalRepository;
  private final MarketRepository marketRepository;
  private final NatureRepository natureRepository;
  private final RestaurantRepository restaurantRepository;
  private final RedisUtil redisUtil;
  private final ObjectMapper objectMapper;
//  private final PostServiceImpl postService;

  @Transactional
  public void increaseViewCount(Long postId, Long memberId) {

    String redisKey = "post_viewed_" + memberId + "_" + postId;

    // 30분 -> 1800초
    long cacheDurationSeconds = 1800L;

    // 30분 이내에 조회한 기록이 없으면
    if (redisUtil.getValue(redisKey) == null) {
      // 조회수 증가
      postRepository.increaseViewCount(postId);
      // 레디스에 등록
      redisUtil.setExpiringValue(redisKey, "viewed", cacheDurationSeconds);
    }

  }

  //인기 게시물 3개 반환
  public List<PopularPostPreviewDto> getLastWeekPopularPosts(MemberInfoDto memberInfoDto)
      throws JsonProcessingException {
    // redis에서 갖고온 후
    List<String> serializedPosts = getJsonPopularPosts(memberInfoDto.getLanguage());
    //직렬화
    return deserializePostPreviewDtos(serializedPosts);

  }

  // 지난 한국 인기 게시물 조회해서 id 추출 -> excludeIds
  // excludeIds 제외한 카테고리 별 조회수 top 3(조회수 0이 아닌) 조회,
  // 다 합친게 3개가 넘지 않으면 -> 부족한 수 만큼 카테고리 랜덤으로 추출, 추출한 카테고리에서 각 한개씩 게시물 선택
  // 최종 나온 게시물 조회수 기준 내림차순 정렬 (한국어 기준으로 만들어짐)
  // 한국어 기준으로 만들어진 인기 게시물 언어별로 조회 후 redis에 저장
  // 월요일마다 실행
  @Scheduled(cron = "0 0 0 * * MON")
  public void setPopularPosts() throws JsonProcessingException {

    // 지난 인기 한국 게시물 json 형태로 json에서 get
    List<String> serializedPosts = getJsonPopularPosts(Language.KOREAN);
    // redis에서 list 자료형은 추가하면 덮어쓰기가 아닌 append이므로 지난 주 인기 게시물 삭제
    deleteAllPopularPostPreviewDtos();

    // json 역직렬화
    List<PopularPostPreviewDto> postPreviewDtos = deserializePostPreviewDtos(serializedPosts);

    // 지난 주 인기 게시물 id -> 제외시킬 게시물
    List<Long> excludeIds = postPreviewDtos.stream()
        .map(PopularPostPreviewDto::getId) // PostPreviewDto의 getId() 메서드 호출
        .collect(Collectors.toList());

    // 카테고리별 top 3 게시물 조회
    List<PopularPostPreviewDto> popularKoreanPostPreviewDtos = new ArrayList<>();
    popularKoreanPostPreviewDtos.addAll(
        experienceRepository.findAllTop3PopularPostPreviewDtoByLanguage(Language.KOREAN));
    popularKoreanPostPreviewDtos.addAll(
        festivalRepository.findAllTop3PopularPostPreviewDtoByLanguage(Language.KOREAN));
    popularKoreanPostPreviewDtos.addAll(
        marketRepository.findAllTop3PopularPostPreviewDtoByLanguage(Language.KOREAN));
    popularKoreanPostPreviewDtos.addAll(
        natureRepository.findAllTop3PopularPostPreviewDtoByLanguage(Language.KOREAN));
    popularKoreanPostPreviewDtos.addAll(
        restaurantRepository.findAllTop3PopularPostPreviewDtoByLanguage(Language.KOREAN));

    // 조회수가 0이 아니고, excludeIds 제외환 게시물이 3개 미달이면
    int postsSize = popularKoreanPostPreviewDtos.size();
    if (postsSize < MAX_POPULAR_POSTS) {
      // 부족한 게시물 수 만큼 카테고리 랜덤 추출
      int extraPostDtoSize = MAX_POPULAR_POSTS - postsSize;
      List<Category> randomCategories = Category.getRandomCategories(extraPostDtoSize);

      // 카테고리 별로 랜덤 게시물(excludeIds 포함하지 않는) 1개씩 조회
      popularKoreanPostPreviewDtos.addAll(getRandomPopularPostPreviewDtosByCategoriesAndLanguage(
          randomCategories, Language.KOREAN, excludeIds));
    }

    // 최종 한국어 인기 게시물 정렬 해서 3개까지
    popularKoreanPostPreviewDtos.sort(
        Comparator.comparing(PopularPostPreviewDto::getViewCount).reversed());
    popularKoreanPostPreviewDtos = popularKoreanPostPreviewDtos.subList(0, 3);

    // address 형태 변환
    popularKoreanPostPreviewDtos = modifyAddressTagToLongForm(popularKoreanPostPreviewDtos,
        Language.KOREAN);
    // 한국 게시물 redis에 저장
    setJsonPopularPosts(Language.KOREAN, serializePostPreviewDtos(popularKoreanPostPreviewDtos));

    // 한국어 게시물을 바탕으로 언어별로 조회
    for (Language language : Language.values()) {
      if (!language.equals(Language.KOREAN)) {
        List<PopularPostPreviewDto> popularPostPreviewDto = getPopularPostPreviewDtosByKoreanPostsAndLanguage(
            popularKoreanPostPreviewDtos, language);
        // address 형태 변환
        popularPostPreviewDto = modifyAddressTagToLongForm(popularPostPreviewDto, language);
        // 레디스에 저장
        setJsonPopularPosts(language, serializePostPreviewDtos(popularPostPreviewDto));
      }
    }


  }

  private List<String> getJsonPopularPosts(Language language) {
    return redisUtil.getAllListValue(POPULAR_POSTS + language.name());
  }

  private void setJsonPopularPosts(Language language, List<String> jsons) {
    redisUtil.setListValue(POPULAR_POSTS + language.name(), jsons);
  }

  private void deleteAllPopularPostPreviewDtos() {
    for (Language language : Language.values()) {
      redisUtil.deleteData(POPULAR_POSTS + language.name());
    }
  }

  // 직렬화
  private List<String> serializePostPreviewDtos(List<PopularPostPreviewDto> postDtos)
      throws JsonProcessingException {
    List<String> jsons = new ArrayList<>();
    if (postDtos != null) {
      for (PopularPostPreviewDto postDto : postDtos) {
        String json = objectMapper.writeValueAsString(postDto); // 객체를 JSON 문자열로 변환
        jsons.add(json);
      }
    }
    return jsons;
  }

  // 역직렬화
  private List<PopularPostPreviewDto> deserializePostPreviewDtos(List<String> jsons)
      throws JsonProcessingException {
    List<PopularPostPreviewDto> postDtos = new ArrayList<>();
    if (jsons != null) {
      for (String serializedPost : jsons) {
        PopularPostPreviewDto postDto = objectMapper.readValue(serializedPost,
            PopularPostPreviewDto.class);
        postDtos.add(postDto);
      }
    }
    return postDtos;
  }

  // 카테고리 별로 언어에 맞게 랜덤으로 게시물 1개씩 뽑기
  private List<PopularPostPreviewDto> getRandomPopularPostPreviewDtosByCategoriesAndLanguage(
      List<Category> categories, Language language, List<Long> excludeIds) {

    List<PopularPostPreviewDto> result = new ArrayList<>();
    // 스트림 기반으로 Category를 순회하며 PopularPostPreviewDto 수집
    for (Category category : categories) {
      switch (category) {
        case FESTIVAL -> result.add(
            festivalRepository.findRandomPopularPostPreviewDtoByLanguage(language, excludeIds));
        case MARKET -> result.add(
            marketRepository.findRandomPopularPostPreviewDtoByLanguage(language, excludeIds));
        case NATURE -> result.add(
            natureRepository.findRandomPopularPostPreviewDtoByLanguage(language, excludeIds));
        case EXPERIENCE -> result.add(
            experienceRepository.findRandomPopularPostPreviewDtoByLanguage(language, excludeIds));
        case RESTAURANT -> result.add(
            restaurantRepository.findRandomPopularPostPreviewDtoByLanguage(language, excludeIds));
        default -> throw new IllegalStateException("Unexpected value: " + category);
      }
    }
    return result;
  }

  // 주어진 한국어 게시물을 기준으로 같은 다른 언어 게시물 조회
  private List<PopularPostPreviewDto> getPopularPostPreviewDtosByKoreanPostsAndLanguage(
      List<PopularPostPreviewDto> koreanPosts, Language language) {
    return koreanPosts.stream()
        .map(postDto -> {
          Long id = postDto.getId();
          Category category = Category.valueOf(postDto.getCategory());
          return switch (category) {
            case FESTIVAL -> festivalRepository.findPostPreviewDtoByLanguageAndId(language, id);
            case MARKET -> marketRepository.findPostPreviewDtoByLanguageAndId(language, id);
            case NATURE -> natureRepository.findPostPreviewDtoByLanguageAndId(language, id);
            case EXPERIENCE -> experienceRepository.findPostPreviewDtoByLanguageAndId(language, id);
            case RESTAURANT -> restaurantRepository.findPostPreviewDtoByLanguageAndId(language, id);
            default -> throw new IllegalStateException("Unexpected value: " + category);
          };
        })
        .collect(Collectors.toList());
  }

  // addressTag를 long form (제주 -> 제주도 제주시)으로 변환
  private List<PopularPostPreviewDto> modifyAddressTagToLongForm(
      List<PopularPostPreviewDto> postDtos, Language language) {
    return postDtos.stream()
        .map(postDto -> {
          String localizedAddressTag = AddressTag.getAddressTagEnum(postDto.getAddress())
              .getLongValueByLocale(language);
          // addressTag를 변환된 값으로 설정
          postDto.setAddress(localizedAddressTag);
          return postDto;

        })
        .collect(Collectors.toList());
  }
}

