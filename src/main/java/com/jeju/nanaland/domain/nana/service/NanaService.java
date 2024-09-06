package com.jeju.nanaland.domain.nana.service;

import static com.jeju.nanaland.domain.common.data.Category.NANA;
import static com.jeju.nanaland.domain.common.data.Category.NANA_CONTENT;
import static com.jeju.nanaland.global.exception.ErrorCode.NANA_TITLE_NOT_FOUND;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.PostCategory;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.common.repository.PostImageFileRepository;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.common.service.PostService;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.hashtag.entity.Hashtag;
import com.jeju.nanaland.domain.hashtag.repository.HashtagRepository;
import com.jeju.nanaland.domain.hashtag.service.HashtagService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnail;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailDto;
import com.jeju.nanaland.domain.nana.entity.InfoType;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaAdditionalInfo;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nana.repository.NanaAdditionalInfoRepository;
import com.jeju.nanaland.domain.nana.repository.NanaContentRepository;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nana.repository.NanaTitleRepository;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.exception.ServerErrorException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  private final ImageFileService imageFileService;
  private final NanaAdditionalInfoRepository nanaAdditionalInfoRepository;
  private final HashtagService hashtagService;
  private final ImageFileRepository imageFileRepository;
  private final PostImageFileRepository postImageFileRepository;

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
   * 카드 정보 조회 - (postId, category, imageFile, title)
   *
   * @param postId   게시물 id
   * @param category 게시물 카테고리
   * @param language 언어 정보
   * @return PostCardDto
   * @throws NotFoundException (게시물 id, langugae)를 가진 나나스픽 정보가 존재하지 않는 경우
   */
  @Override
  public PostCardDto getPostCardDto(Long postId, Category category, Language language) {
    PostCardDto postCardDto = nanaRepository.findPostCardDto(postId, language);

    // 게시물 정보가 없는 경우 에러처리
    Optional.ofNullable(postCardDto)
        .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));

    postCardDto.setCategory(PostCategory.NANA.toString());
    return postCardDto;
  }

  // 메인페이지에 보여지는 4개의 nana
  public List<NanaThumbnail> getMainNanaThumbnails(Language locale) {
    return nanaRepository.findRecentNanaThumbnailDto(locale);
  }

  // 나나's pick 금주 추천 게시글 4개 (modifiedAt 으로 최신순 4개)
  public List<NanaThumbnail> getRecommendNanaThumbnails(Language locale) {
    List<NanaThumbnail> recommendNanaThumbnailDto = nanaRepository.findRecommendNanaThumbnailDto(
        locale);
    markNewestThumbnails(recommendNanaThumbnailDto);
    return recommendNanaThumbnailDto;
  }


  // 나나's pick 썸네일 리스트 조회
  public NanaThumbnailDto getNanaThumbnails(Language locale, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<NanaThumbnail> resultDto = nanaRepository.findAllNanaThumbnailDto(locale,
        pageable);
    List<NanaThumbnail> resultDtoContent = resultDto.getContent();

    // new 태그 붙일지 말지 결정
    markNewestThumbnails(resultDtoContent);

    return NanaThumbnailDto.builder()
        .totalElements(resultDto.getTotalElements())
        .data(resultDtoContent)
        .build();
  }

  //나나 상세 게시물
  public NanaResponse.NanaDetailDto getNanaDetail(MemberInfoDto memberInfoDto, Long postId,
      boolean isSearch) {

    Language language = memberInfoDto.getLanguage();

    // nana 찾아서
    Nana nana = getNanaById(postId);

    // nanaTitle 찾아서
    NanaTitle nanaTitle = nanaTitleRepository.findNanaTitleByNanaAndLanguage(nana, language)
        .orElseThrow(() -> new NotFoundException(NANA_TITLE_NOT_FOUND.getMessage()));

    NanaTitle koreanNanaTitle;
    List<NanaContent> nanaContentList;
    List<NanaContent> koreanNanaContentList;

    // korean nanaTitle 찾기 -> nanaContent의 이미지는 korean nanaContent의 이미지를 공유하기 때문에 찾아놔야함
    if (language == Language.KOREAN) {
      nanaContentList = nanaContentRepository.findAllByNanaTitleOrderByPriority(nanaTitle);
      koreanNanaContentList = nanaContentList;
    } else {
      koreanNanaTitle = nanaTitleRepository.findNanaTitleByNanaAndLanguage(nana, Language.KOREAN)
          .orElseThrow(() -> new NotFoundException(NANA_TITLE_NOT_FOUND.getMessage()));
      nanaContentList = nanaContentRepository.findAllByNanaTitleOrderByPriority(nanaTitle);
      koreanNanaContentList = nanaContentRepository.findAllByNanaTitleOrderByPriority(
          koreanNanaTitle);
    }

    if (isSearch) {
      searchService.updateSearchVolumeV1(NANA, nana.getId());
    }

    // nanaTitle에 맞는 nanaContent게시물 조회, <- 에 사용할 nanaContent 이미지 koreanNanaContent에서 찾기

    // nanaContent 별 이미지 리스트 조회 후 저장하기.
    List<List<ImageFileDto>> nanaContentImageList = new ArrayList<>();

    for (NanaContent nanaContent : koreanNanaContentList) { // 각 korean nanaContent에 맞는 사진들 가져오기

      // content의 이미지 postImageFile에서 여러 개 찾아오기
      List<ImageFileDto> contentImageFiles = new ArrayList<>(imageFileRepository.findPostImageFiles(
          nanaContent.getId()));

      if (contentImageFiles.isEmpty()) {// 사진 없으면 서버 에러
        throw new ServerErrorException(ErrorCode.SERVER_ERROR.getMessage());
      }
      nanaContentImageList.add(contentImageFiles);
    }

    // 서버 오류 체크 (밑에 나오는 for문을 실행하기 위해서는 nanaContentList와 nanaContentImageList의 수 일치해야함)
    if (nanaContentList.size() != nanaContentImageList.size()) {
      throw new ServerErrorException("나나's pick의 content와 image의 수가 일치하지 않습니다.");
    }

    boolean isPostInFavorite = memberFavoriteService.isPostInFavorite(memberInfoDto.getMember(),
        NANA, nanaTitle.getNana().getId());

    List<NanaResponse.NanaDetail> nanaDetails = new ArrayList<>();
    int nanaContentImageIdx = 0;
    for (NanaContent nanaContent : nanaContentList) {

      List<Hashtag> hashtagList = hashtagRepository.findAllByLanguageAndCategoryAndPostId(
          language, NANA_CONTENT, nanaContent.getId());

      // 해시태그 정보 keyword 가져와서 list 형태로 바꾸기
      List<String> stringKeywordList = getStringKeywordListFromHashtagList(hashtagList);

      nanaDetails.add(
          NanaResponse.NanaDetail.builder()
              .number(nanaContent.getPriority().intValue())
              .subTitle(nanaContent.getSubTitle())
              .title(nanaContent.getTitle())
              .images(nanaContentImageList.get(nanaContentImageIdx))
//              .imageUrl(nanaContentImageList.get(nanaContentImageIdx).getImageFile().getOriginUrl())
              .content(nanaContent.getContent())
              .additionalInfoList(
                  getAdditionalInfoFromNanaContentEntity(language, nanaContent))
              .hashtags(stringKeywordList)
              .build());
      nanaContentImageIdx++;
    }

    return NanaResponse.NanaDetailDto.builder()
        .id(nana.getId())
        .firstImage(new ImageFileDto(nana.getFirstImageFile().getOriginUrl(),
            nana.getFirstImageFile().getThumbnailUrl()))
        .subHeading(nanaTitle.getSubHeading())
        .heading(nanaTitle.getHeading())
        .version(nana.getVersion())
        .notice(nanaTitle.getNotice())
        .nanaDetails(nanaDetails)
        .isFavorite(isPostInFavorite)
        .build();

  }

  // upload 하는 페이지에서 존재하는 나나스핔 id - 언어별 보여주기
  public Map<Long, List<String>> getExistNanaListInfo() {
    // id - List<Language> 형태의 해쉬
    HashMap<Long, List<String>> result = new HashMap<>();
    List<NanaTitle> nanaTitles = nanaTitleRepository.findAll();

    // 없는 key 값이면 (for 돌아가면서 처음나온 나나스핔이면) id 값과 langauge 새로추가
    // 있는 key 면 해당 key에 List<Language> (value)에 Language 추가
    for (NanaTitle nanaTitle : nanaTitles) {
      Long id = nanaTitle.getNana().getId();
      String language = nanaTitle.getLanguage().toString();

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

//  // 나나스픽 생성
//  // TODO : 모듈화한 부분이니 제거해도 괜찮을 것 같아보임
//  @Transactional
//  public String createNanaPick(NanaRequest.NanaUploadDto nanaUploadDto) {
//    try {
//      boolean existNanaContentImages = false;
//      Nana nana;
//      Long nanaId = nanaUploadDto.getPostId();
//      Language language = Language.valueOf(nanaUploadDto.getLanguage());
//      Category category = NANA_CONTENT;
//
//      // 없는 nana이면 nana 만들기
//      if (!existNanaById(nanaId)) {
//
//        // 처음 생성되는 나나's pick이면 KOREAN 버전부터 생성되어야 함
//        if (!Objects.equals(nanaUploadDto.getLanguage(), "KOREAN")) {
//          throw new BadRequestException("처음 생성하는 Nana's pick은 KOREAN 버전부터 입력해야합니다.");
//        }
//        // 처음 생성하는 nana인데 title image 없을 경우 에러
//        if (nanaUploadDto.getNanaTitleImage().isEmpty()) {
//          throw new BadRequestException("처음 생성하는 Nana's pick에는 title 이미지가 필수입니다.");
//        }
//        // nana 생성해서 저장하기
//        nana = createNanaByNanaUploadDto(nanaUploadDto);
//      } else {// 이미 존재하는 nana인 경우
//        nana = getNanaById(nanaId);
//
//        //존재하는 nana일 경우 해당 post가 이미 작성된 language로 요청이 올 경우 에러
//        if (existNanaTitleByNanaAndLanguage(nana, language)) {
//          throw new BadRequestException("이미 존재하는 NanaTitle의 Language입니다");
//        }
//
//        // 이미 nanaTitle이 존재하면 nanaContentImage 추가 생성 필요 없음을 표시
//        if (existNanaTitleByNana(nana)) {
//          existNanaContentImages = true;
//        }
//
//        /*
//         * 이미 존재하는 경우 한국어 버전 NanaContent의 수와 비교한다.
//         * (nanaContent들은 KOREAN nana Content의 사진들 공유 하기 때문에 기준은 KOREAN 버전)
//         * 기존의 content 수와 새로 요청한 content 게시글 수가 일치하지 않을 때
//         */
//        if (countKoreanNanaContents(nana)
//            != nanaUploadDto.getNanaContents().size()) {
//          throw new BadRequestException(
//              "기존의 nana content 수와 현재 요청한 nana content의 수가 일치하지 않습니다.");
//        }
//      }
//
//      NanaTitle nanaTitle = NanaTitle.builder()
//          .nana(nana)
//          .language(language)
//          .subHeading(nanaUploadDto.getSubHeading())
//          .heading(nanaUploadDto.getHeading())
//          .notice(nanaUploadDto.getNotice())
//          .build();
//      nanaTitleRepository.save(nanaTitle);
//
////      람다 안에서 외부 변수를 사용하기 위해서는 effectively final 변수를 사용해야하므로 선언
////      존재할 경우 flag -> false / 존재하지 않을 경우 flag -> true
//      boolean createNanaContentsImageFlag = !existNanaContentImages;
//
//      // 람다 시작
//      //content 생성
//      nanaUploadDto.getNanaContents().forEach(nanaContentDto -> {
//            NanaContent nanaContent = nanaContentRepository.save(NanaContent.builder()
//                .nanaTitle(nanaTitle)
//                .priority((long) nanaContentDto.getNumber())
//                .subTitle(nanaContentDto.getSubTitle())
//                .title(nanaContentDto.getTitle())
//                .content(nanaContentDto.getContent())
//                .infoList(createNanaAdditionalInfo(nanaContentDto.getAdditionalInfo(),
//                    nanaContentDto.getInfoDesc()))
//                .build());
//
//            if (createNanaContentsImageFlag) { // nanaContentImage가 존재하지 않을 경우에만 추가.
//              // 람다 안에 들어있으니 nanaContent 1개에 대한 사진 묶음
//              List<MultipartFile> nanaContentImages = nanaContentDto.getNanaContentImages();
//              if (nanaContentImages.isEmpty()) {
//                throw new BadRequestException("처음 생성하는 Nana's pick에는 content 이미지가 필수입니다. ");
//              }
//              // nanaContent 하나 당 여러 개의 이미지 처리
//              List<PostImageFile> postImageFiles = nanaContentImages.stream()
//                  .map(image -> PostImageFile.builder()
//                      .imageFile(imageFileService.uploadAndSaveImageFile(image, false))
//                      .post(nanaContent)
//                      .build())
//                  .collect(Collectors.toList());
//
//              postImageFileRepository.saveAll(postImageFiles);
//            }
//
//            //해시태그 생성
//            hashtagService.registerHashtag(nanaContentDto.getHashtag(),
//                language, category, nanaContent);
//          } // 람다 끝
//      );
//    } catch (Exception e) {
//      // 외부 메서드 호출 시 에러 터지면 롤백
//      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//      return e.getMessage();
//    }
//    return "성공~";
//
//  }

  // nanaContent의 AdditionalInfo dto로 바꾸기
  private List<NanaResponse.NanaAdditionalInfo> getAdditionalInfoFromNanaContentEntity(
      Language locale, NanaContent nanaContent) {
    List<NanaResponse.NanaAdditionalInfo> result = new ArrayList<>();
    Set<NanaAdditionalInfo> eachInfoList = nanaContent.getInfoList();

    // 순서 보장 위해 List 형으로 바꾸고 생성 순서로 정렬
    if (eachInfoList != null) {
      List<NanaAdditionalInfo> nanaAdditionalInfos = new ArrayList<>(eachInfoList);
      nanaAdditionalInfos.sort(Comparator.comparing(NanaAdditionalInfo::getCreatedAt));

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

  // 키워드 리스트 반환
  private List<String> getStringKeywordListFromHashtagList(List<Hashtag> hashtagList) {
    return hashtagList.stream()
        .map(hashtag -> hashtag.getKeyword().getContent())
        .collect(Collectors.toList());
  }

  private boolean existNanaById(Long id) {
    //id가 0(생성하는 경우) 또는 존재하지 않는 나나의 id일 경우 false
    //id가 0이 아니고 존재하는 id일 경우 true
    return id != 0 && nanaRepository.existsById(id);
  }

  private boolean existNanaTitleByNana(Nana nana) {
    return nanaTitleRepository.existsByNana(nana);
  }

  private boolean existNanaTitleByNanaAndLanguage(Nana nana, Language language) {
    return nanaTitleRepository.existsByNanaAndLanguage(nana, language);
  }

//  private Nana createNanaByNanaUploadDto(NanaRequest.NanaUploadDto nanaUploadDto) {
//    return Nana.builder()
//        .version("Nana's Pick vol." + nanaUploadDto.getVersion())
//        .priority((long) nanaUploadDto.getVersion())
//        .firstImageFile(imageFileService.uploadAndSaveImageFile(nanaUploadDto.getNanaTitleImage(),
//            false))
//        .build();
//  }

  private Nana getNanaById(Long id) {
    return nanaRepository.findNanaById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.NANA_NOT_FOUND.getMessage()));

  }

  private int countKoreanNanaContents(Nana nana) {
    NanaTitle nanaTitle = nanaTitleRepository.findNanaTitleByNanaAndLanguage(nana, Language.KOREAN)
        .orElseThrow(() -> new ServerErrorException(NANA_TITLE_NOT_FOUND.getMessage()));
    return nanaContentRepository.countNanaContentByNanaTitle(nanaTitle);
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

  // new 태그 붙일지 말지 결정
  // 가장 최신 게시물에 new
  private void markNewestThumbnails(List<NanaThumbnail> thumbnails) {
    thumbnails.stream()
        .max(Comparator.comparing(NanaThumbnail::getCreatedAt))
        .ifPresent(thumbnail -> thumbnail.setNewest(true));
  }
}
