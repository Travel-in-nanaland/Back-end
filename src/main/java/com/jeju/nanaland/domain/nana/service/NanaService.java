package com.jeju.nanaland.domain.nana.service;

import static com.jeju.nanaland.domain.common.data.CategoryContent.NANA;
import static com.jeju.nanaland.domain.common.data.CategoryContent.NANA_CONTENT;

import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.repository.CategoryRepository;
import com.jeju.nanaland.domain.common.repository.LanguageRepository;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.hashtag.entity.Hashtag;
import com.jeju.nanaland.domain.hashtag.repository.HashtagRepository;
import com.jeju.nanaland.domain.hashtag.service.HashtagService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nana.dto.NanaRequest;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnail;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailDto;
import com.jeju.nanaland.domain.nana.entity.InfoType;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaAdditionalInfo;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaContentImage;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nana.repository.NanaAdditionalInfoRepository;
import com.jeju.nanaland.domain.nana.repository.NanaContentImageRepository;
import com.jeju.nanaland.domain.nana.repository.NanaContentRepository;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nana.repository.NanaTitleRepository;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.exception.ServerErrorException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
public class NanaService {

  private final CategoryRepository categoryRepository;
  private final NanaRepository nanaRepository;
  private final NanaTitleRepository nanaTitleRepository;
  private final NanaContentRepository nanaContentRepository;
  private final HashtagRepository hashtagRepository;
  private final FavoriteService favoriteService;
  private final SearchService searchService;
  private final ImageFileService imageFileService;
  private final LanguageRepository languageRepository;
  private final NanaContentImageRepository nanaContentImageRepository;
  private final NanaAdditionalInfoRepository nanaAdditionalInfoRepository;
  private final HashtagService hashtagService;

  //메인페이지에 보여지는 4개의 nana
  public List<NanaThumbnail> getMainNanaThumbnails(Locale locale) {
    return nanaRepository.findRecentNanaThumbnailDto(locale);
  }

  //나나 들어갔을 때 보여줄 모든 nana
  public NanaThumbnailDto getNanaThumbnails(Locale locale, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<NanaThumbnail> resultDto = nanaRepository.findAllNanaThumbnailDto(locale,
        pageable);

    List<NanaThumbnail> thumbnails = new ArrayList<>();
    for (NanaThumbnail dto : resultDto) {
      thumbnails.add(
          NanaThumbnail.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .version(dto.getVersion())
              .subHeading(dto.getSubHeading())
              .heading(dto.getHeading())
              .build());
    }
    return NanaThumbnailDto.builder()
        .totalElements(resultDto.getTotalElements())
        .data(thumbnails)
        .build();
  }

  //나나 상세 게시물
  public NanaResponse.NanaDetailDto getNanaDetail(MemberInfoDto memberInfoDto, Long nanaId,
      boolean isSearch) {

    Language language = memberInfoDto.getLanguage();

    // nana 찾아서
    Nana nana = nanaRepository.findNanaById(nanaId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.NANA_NOT_FOUND.getMessage()));

    // nanaTitle 찾아서
    NanaTitle nanaTitle = nanaTitleRepository.findNanaTitleByNanaAndLanguage(nana,
            language)
        .orElseThrow(() -> new NotFoundException(ErrorCode.NANA_TITLE_NOT_FOUND.getMessage()));

    if (isSearch) {
      searchService.updateSearchVolumeV1(NANA, nana.getId());
    }

    // nanaTitle에 맞는 nanaContent게시물 조회, nanaContent에 맞는 image 정렬
    List<NanaContent> nanaContentList = nanaContentRepository.findAllByNanaTitleOrderByNumber(
        nanaTitle);
    List<NanaContentImage> nanaContentImageList = nana.getNanaContentImageList().stream()
        .sorted(Comparator.comparingInt(NanaContentImage::getNumber))
        .toList();

    // 서버 오류 체크 (밑에 나오느 for문을 실행하기 위해서는 nanaContentList와 nanaContentImageList의 수 일치해야함)
    if (nanaContentList.size() != nanaContentImageList.size()) {
      throw new ServerErrorException("나나's pick의 content와 image의 수가 일치하지 않습니다.");
    }

    boolean isPostInFavorite = favoriteService.isPostInFavorite(memberInfoDto.getMember(), NANA,
        nanaTitle.getNana().getId());

    Category category = categoryRepository.findByContent(NANA_CONTENT)
        .orElseThrow(() -> new ServerErrorException("NANA_CONTENT에 해당하는 카테고리가 없습니다."));

    List<NanaResponse.NanaDetail> nanaDetails = new ArrayList<>();
    int nanaContentImageIdx = 0;
    for (NanaContent nanaContent : nanaContentList) {

      List<Hashtag> hashtagList = hashtagRepository.findAllByLanguageAndCategoryAndPostId(
          language, category, nanaContent.getId());

      // 해시태그 정보 keyword 가져와서 list 형태로 바꾸기
      List<String> stringKeywordList = getStringKeywordListFromHashtagList(hashtagList);

      nanaDetails.add(
          NanaResponse.NanaDetail.builder()
              .number(nanaContent.getNumber())
              .subTitle(nanaContent.getSubTitle())
              .title(nanaContent.getTitle())
              .imageUrl(nanaContentImageList.get(nanaContentImageIdx).getImageFile().getOriginUrl())
              .content(nanaContent.getContent())
              .additionalInfoList(
                  getAdditionalInfoFromNanaContentEntity(language.getLocale(), nanaContent))
              .hashtags(stringKeywordList)
              .build());
      nanaContentImageIdx++;
    }

    return NanaResponse.NanaDetailDto.builder()
        .originUrl(nana.getNanaTitleImageFile().getOriginUrl())
        .subHeading(nanaTitle.getSubHeading())
        .heading(nanaTitle.getHeading())
        .version(nana.getVersion())
        .notice(nanaTitle.getNotice())
        .nanaDetails(nanaDetails)
        .isFavorite(isPostInFavorite)
        .build();

  }

  public HashMap<Long, List<String>> getExistNanaListInfo() {
    HashMap<Long, List<String>> result = new HashMap<>();
    List<Nana> nanas = nanaRepository.findAll();
    List<NanaTitle> nanaTitles = nanaTitleRepository.findAll();
    for (NanaTitle nanaTitle : nanaTitles) {
      Long id = nanaTitle.getNana().getId();
      String language = nanaTitle.getLanguage().getLocale().toString();

      if (result.containsKey(id)) {
        result.get(id).add(language);
      } else {
        List<String> values = new ArrayList<>();
        values.add(language);
        result.put(id, values);
      }
    }
    return result;
  }

  @Transactional
  public String createNanaPick(NanaRequest.NanaUploadDto nanaUploadDto) {
    try {
      Nana nana;
      Language language = languageRepository.findByLocale(
          Locale.contains(nanaUploadDto.getLanguage()));
      Category category = categoryRepository.findByContent(NANA_CONTENT)
          .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다."));
      // 없는 nana이면 nana 만들기
      if (!existNana(nanaUploadDto.getPostId())) {
        nana = Nana.builder()
            .version("나나's Pick vol." + nanaUploadDto.getVersion())
            .nanaTitleImageFile(
                imageFileService.uploadAndSaveImageFile(nanaUploadDto.getNanaTitleImage(), false))
            .build();
        nanaRepository.save(nana);
      } else {
        Optional<Nana> nanaById = nanaRepository.findNanaById(nanaUploadDto.getPostId());
        nana = nanaById.orElseThrow(
            () -> new NotFoundException(ErrorCode.NANA_NOT_FOUND.getMessage()));
        if (existNanaTitleByLanguage(nana, language)) {
          throw new BadRequestException("이미 존재하는 NanaTitle의 Language입니다");
        }
      }

      NanaTitle nanaTitle = NanaTitle.builder()
          .nana(nana)
          .language(language)
          .subHeading(nanaUploadDto.getSubHeading())
          .heading(nanaUploadDto.getHeading())
          .notice(nanaUploadDto.getNotice())
          .build();
      nanaTitleRepository.save(nanaTitle);

      nanaUploadDto.getNanaContents().forEach(nanaContentDto -> {
            NanaContent nanaContent = nanaContentRepository.save(NanaContent.builder()
                .nanaTitle(nanaTitle)
                .number(nanaContentDto.getNumber())
                .subTitle(nanaContentDto.getSubTitle())
                .title(nanaContentDto.getTitle())
                .content(nanaContentDto.getContent())
                .infoList(createNanaAdditionalInfo(nanaContentDto.getAdditionalInfo(),
                    nanaContentDto.getInfoDesc()))
                .build());
            nanaContentImageRepository.save(
                NanaContentImage.builder()
                    .nana(nana)
                    .imageFile(
                        imageFileService.uploadAndSaveImageFile(nanaContentDto.getNanaContentImage(),
                            false))
                    .number(nanaContentDto.getNumber())
                    .build()
            );

            hashtagService.registerHashtag(splitHashtagContentFromString(nanaContentDto.getHashtag()),
                language, category, nanaContent.getId());


          }

      );
    } catch (Exception e) {
      return e.getMessage();
    }
    return "성공~";

  }

  // nanaContent의 AdditionalInfo dto로 바꾸기
  private List<NanaResponse.NanaAdditionalInfo> getAdditionalInfoFromNanaContentEntity(
      Locale locale, NanaContent nanaContent) {
    Set<NanaAdditionalInfo> eachInfoList = nanaContent.getInfoList();

    // 순서 보장 위해 List 형으로 바꾸고
    List<NanaAdditionalInfo> nanaAdditionalInfos = new ArrayList<>(eachInfoList);

    //DTO 형태로 변환
    List<NanaResponse.NanaAdditionalInfo> result = new ArrayList<>();
    for (NanaAdditionalInfo info : nanaAdditionalInfos) {
      result.add(NanaResponse.NanaAdditionalInfo.builder()
          .infoEmoji(info.getInfoType().toString())
          .infoKey(info.getInfoType().getValueByLocale(locale))
          .infoValue(info.getDescription())
          .build());
    }
    return result;
  }

  private List<String> getStringKeywordListFromHashtagList(List<Hashtag> hashtagList) {
    return hashtagList.stream()
        .map(hashtag -> hashtag.getKeyword().getContent())
        .collect(Collectors.toList());
  }

  private boolean existNana(Long id) {
    return nanaRepository.existsById(id);
  }

  private boolean existNanaTitleByLanguage(Nana nana, Language language) {
    return nanaTitleRepository.existsByNanaAndLanguage(nana, language);
  }

  private Set<NanaAdditionalInfo> createNanaAdditionalInfo(List<String> infoTypeList,
      List<String> descriptionList) {
    if (infoTypeList.size() != descriptionList.size()) {
      throw new BadRequestException("nana Upload 중 infoTye과 description의 수가 일치하지 않습니다.");
    }
    Set<NanaAdditionalInfo> nanaAdditionalInfoSet = new HashSet<>();

    for (int i = 0; i < infoTypeList.size(); i++) {
      nanaAdditionalInfoSet.add(
          nanaAdditionalInfoRepository.save(
              NanaAdditionalInfo.builder()
                  .infoType(InfoType.contains(infoTypeList.get(i)))
                  .description(descriptionList.get(i))
                  .build())
      );
    }
    return nanaAdditionalInfoSet;
  }

  private List<String> splitHashtagContentFromString(String content) {
    List<String> strings = new ArrayList<>(Arrays.asList(content.split("\\s+")));
    for (String string : strings) {
      System.out.println("string = " + string);
    }
    return strings;
  }
}
