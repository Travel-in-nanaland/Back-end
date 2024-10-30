package com.jeju.nanaland.domain.nana.service;

import static com.jeju.nanaland.domain.common.data.Category.NANA;
import static com.jeju.nanaland.domain.common.data.Category.NANA_CONTENT;
import static com.jeju.nanaland.global.exception.ErrorCode.NANA_TITLE_NOT_FOUND;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.PostCategory;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.common.service.PostService;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.hashtag.entity.Hashtag;
import com.jeju.nanaland.domain.hashtag.repository.HashtagRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaAdditionalInfo;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nana.repository.NanaContentRepository;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nana.repository.NanaTitleRepository;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.exception.ServerErrorException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NanaService implements PostService {

  private final NanaRepository nanaRepository;
  private final NanaTitleRepository nanaTitleRepository;
  private final NanaContentRepository nanaContentRepository;
  private final HashtagRepository hashtagRepository;
  private final MemberFavoriteService memberFavoriteService;
  private final SearchService searchService;
  private final ImageFileRepository imageFileRepository;


  /**
   * Nana 객체 조회
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @return Post
   * @throws NotFoundException 게시물 id에 해당하는 나나스픽 게시물이 존재하지 않는 경우
   */
  @Override
  public Post getPost(Long postId, Category category) {
    return nanaRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));
  }

  /**
   * 게시물 preview 정보 조회 - (postId, category, imageFile, title)
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @param language 언어 정보
   * @return PostPreviewDto
   * @throws NotFoundException (게시물 id, langugae)를 가진 나나스픽 정보가 존재하지 않는 경우
   */
  @Override
  public PostPreviewDto getPostPreviewDto(Long postId, Category category, Language language) {
    PostPreviewDto postPreviewDto = nanaRepository.findPostPreviewDto(postId, language);
    Optional.ofNullable(postPreviewDto)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));

    postPreviewDto.setCategory(PostCategory.NANA.toString());
    return postPreviewDto;
  }

  /**
   * 메인페이지에 보여지는 최신 4개의 nana
   *
   * @param locale 검색 지역 조건
   * @return
   */
  public List<NanaResponse.PreviewDto> getMainPageNanaThumbnails(Language locale) {
    return nanaRepository.findTop4PreviewDtoOrderByCreatedAt(locale);
  }

  /**
   * 나나's pick 금주 추천 게시글 4개 (modifiedAt 으로 최신순 4개)
   *
   * @param locale 검색 지역 조건
   * @return
   */
  public List<NanaResponse.PreviewDto> getRecommendNanaThumbnails(Language locale) {
    List<NanaResponse.PreviewDto> recommendPreviewDtoDto = nanaRepository.findRecommendPreviewDto(
        locale);
    markNewestThumbnails(recommendPreviewDtoDto);
    return recommendPreviewDtoDto;
  }

  /**
   * 나나's pick 썸네일 리스트 조회
   *
   * @param locale
   * @param page
   * @param size
   * @return
   */
  public NanaResponse.PreviewPageDto getNanaThumbnails(Language locale, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<NanaResponse.PreviewDto> resultDto = nanaRepository.findAllPreviewDtoOrderByCreatedAt(
        locale,
        pageable);
    List<NanaResponse.PreviewDto> resultDtoContent = resultDto.getContent();

    // new 태그 붙일지 말지 결정
    markNewestThumbnails(resultDtoContent);

    return NanaResponse.PreviewPageDto.builder()
        .totalElements(resultDto.getTotalElements())
        .data(resultDtoContent)
        .build();
  }

  /**
   * 나나 상세 게시물. nanaContent는 post에 firstImage를 갖지 않는다, nanaContent는 KoreanNanaContent의 이미지를 공유한다.
   *
   * @param memberInfoDto
   * @param postId
   * @param isSearch
   * @return
   */

  public NanaResponse.DetailPageDto getNanaDetail(MemberInfoDto memberInfoDto, Long postId,
      boolean isSearch) {

    Language language = memberInfoDto.getLanguage();

    // nana 찾아서
    Nana nana = nanaRepository.findNanaById(postId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.NANA_NOT_FOUND.getMessage()));

    // nanaTitle 찾아서
    NanaTitle nanaTitle = nanaTitleRepository.findNanaTitleByNanaAndLanguage(nana, language)
        .orElseThrow(() -> new NotFoundException(NANA_TITLE_NOT_FOUND.getMessage()));

    // nanaContets 찾아서
    List<NanaContent> nanaContents = nanaContentRepository.findAllByNanaTitleOrderByPriority(
        nanaTitle);

    // nanaContent는 KOREAN 버전의 nanaContents의 이미지를 공유
    List<NanaContent> koreanNanaContents = getKoreanNanaContents(language, nana, nanaContents);

    if (isSearch) {
      searchService.updateSearchVolumeV1(NANA, nana.getId());
    }

    // nanaContent 별 이미지 리스트 조회 후 저장하기.
    List<List<ImageFileDto>> eachNanaContentImages = getEachNanaContentsImages(koreanNanaContents);

    // 서버 오류 체크 (밑에 나오는 for문을 실행하기 위해서는 nanaContentList와 nanaContentImageList의 수 일치해야함)
    if (nanaContents.size() != eachNanaContentImages.size()) {
      throw new ServerErrorException("나나's pick의 content와 image의 수가 일치하지 않습니다.");
    }

    boolean isPostInFavorite = memberFavoriteService.isPostInFavorite(memberInfoDto.getMember(),
        NANA, nanaTitle.getNana().getId());

    List<NanaResponse.ContentDetailDto> contentDetailDtos = getNanaDetailDtosFromNanaContents(
        nanaContents, language, eachNanaContentImages);

    return NanaResponse.DetailPageDto.builder()
        .id(nana.getId())
        .firstImage(new ImageFileDto(nana.getFirstImageFile().getOriginUrl(),
            nana.getFirstImageFile().getThumbnailUrl()))
        .subHeading(nanaTitle.getSubHeading())
        .heading(nanaTitle.getHeading())
        .version(nana.getVersion())
        .notice(nanaTitle.getNotice())
        .nanaDetails(contentDetailDtos)
        .isFavorite(isPostInFavorite)
        .build();

  }

  /**
   * nanaContent Entity의 AdditionalInfo를 dto로 바꾸기
   *
   * @param locale
   * @param nanaContent
   * @return
   */
  private List<NanaResponse.NanaAdditionalInfo> getAdditionalInfoFromNanaContentEntity(
      Language locale, NanaContent nanaContent) {
    List<NanaResponse.NanaAdditionalInfo> result = new ArrayList<>();
    Set<NanaAdditionalInfo> eachInfoList = nanaContent.getInfoList();

    // 순서 보장 위해 List 형으로 바꾸고 생성 순서로 정렬
    if (eachInfoList != null) {
      List<NanaAdditionalInfo> nanaAdditionalInfos = new ArrayList<>(eachInfoList);
      // NanaAdditionalInfo에 작성된 순으로 정렬
      nanaAdditionalInfos.sort(Comparator.comparing(info -> info.getInfoType().ordinal()));

      //DTO 형태로 변환
      for (NanaAdditionalInfo info : nanaAdditionalInfos) {
        result.add(NanaResponse.NanaAdditionalInfo.builder()
            .infoEmoji(info.getInfoType().toString())
            .infoKey(info.getInfoType().getValueByLocale(locale))
            .infoValue(info.getDescription())
            .build());
      }
    }
    return result;
  }

  /**
   * 해시태그(엔티티) 리스트를 문자열 리스트로 변경
   *
   * @param hashtags
   * @return
   */
  // 키워드 리스트 반환
  private List<String> getStringKeywordsFromHashtags(List<Hashtag> hashtags) {
    return hashtags.stream()
        .map(hashtag -> hashtag.getKeyword().getContent())
        .collect(Collectors.toList());
  }

  /**
   * nanaContents -> nanaDetailDtos 변환
   *
   * @param nanaContents
   * @param language
   * @param eachNanaContentImages
   * @return
   */
  private List<NanaResponse.ContentDetailDto> getNanaDetailDtosFromNanaContents(
      List<NanaContent> nanaContents, Language language,
      List<List<ImageFileDto>> eachNanaContentImages) {
    List<NanaResponse.ContentDetailDto> result = new ArrayList<>();
    int nanaContentImageIdx = 0;
    for (NanaContent nanaContent : nanaContents) {

      List<Hashtag> hashtags = hashtagRepository.findAllByLanguageAndCategoryAndPostId(
          language, NANA_CONTENT, nanaContent.getId());

      // 해시태그 정보 keyword 가져와서 list 형태로 바꾸기
      List<String> stringKeywords = getStringKeywordsFromHashtags(hashtags);

      result.add(
          NanaResponse.ContentDetailDto.builder()
              .number(nanaContent.getPriority().intValue())
              .subTitle(nanaContent.getSubTitle())
              .title(nanaContent.getTitle())
              .images(eachNanaContentImages.get(nanaContentImageIdx))
//              .imageUrl(eachNanaContentImages.get(nanaContentImageIdx).getImageFile().getOriginUrl())
              .content(nanaContent.getContent())
              .additionalInfoList(
                  getAdditionalInfoFromNanaContentEntity(language, nanaContent))
              .hashtags(stringKeywords)
              .build());
      nanaContentImageIdx++;
    }
    return result;
  }

  /**
   * nanaContent 순서대로 각 게시물의 이미지 리스트 koreanContents에서 가져오기
   *
   * @param koreanNanaContents
   * @return
   */
  private List<List<ImageFileDto>> getEachNanaContentsImages(List<NanaContent> koreanNanaContents) {
    List<List<ImageFileDto>> eachNanaContentImages = new ArrayList<>();

    for (NanaContent nanaContent : koreanNanaContents) { // 각 korean nanaContent에 맞는 사진들 가져오기

      // content의 이미지 postImageFile에서 여러 개 찾아오기
      List<ImageFileDto> contentImageFiles = new ArrayList<>(imageFileRepository.findPostImageFiles(
          nanaContent.getId()));

      if (contentImageFiles.isEmpty()) {// 사진 없으면 서버 에러
        throw new ServerErrorException(ErrorCode.SERVER_ERROR.getMessage());
      }
      eachNanaContentImages.add(contentImageFiles);
    }
    return eachNanaContentImages;
  }

  /**
   * korean nanaTitle 찾기. nanaContent의 이미지는 korean nanaContent의 이미지를 공유하기 때문에 찾아놔야함
   *
   * @param language
   * @param nana
   * @param nanaContents
   * @return
   */
  private List<NanaContent> getKoreanNanaContents(Language language, Nana nana,
      List<NanaContent> nanaContents) {
    List<NanaContent> koreanNanaContents;
    if (language == Language.KOREAN) {
      koreanNanaContents = nanaContents;
    } else {
      NanaTitle koreanNanaTitle = nanaTitleRepository.findNanaTitleByNanaAndLanguage(nana,
              Language.KOREAN)
          .orElseThrow(() -> new NotFoundException(NANA_TITLE_NOT_FOUND.getMessage()));
      koreanNanaContents = nanaContentRepository.findAllByNanaTitleOrderByPriority(
          koreanNanaTitle);
    }
    return koreanNanaContents;
  }

  /**
   * new 태그 붙일지 말지 결정. 현재 정책은 가장 최신 게시물에 new
   *
   * @param thumbnails
   */
  private void markNewestThumbnails(List<NanaResponse.PreviewDto> thumbnails) {
    thumbnails.stream()
        .max(Comparator.comparing(NanaResponse.PreviewDto::getCreatedAt))
        .ifPresent(thumbnail -> thumbnail.setNewest(true));
  }
}
