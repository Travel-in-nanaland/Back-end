package com.jeju.nanaland.domain.nana.service;

import static com.jeju.nanaland.domain.common.data.CategoryContent.NANA;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnail;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NanaService {

  private final NanaRepository nanaRepository;
  private final NanaTitleRepository nanaTitleRepository;
  private final NanaContentRepository nanaContentRepository;
  private final FavoriteService favoriteService;
  private final SearchService searchService;

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

    // nana 찾아서
    Nana nana = nanaRepository.findNanaById(nanaId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.NANA_NOT_FOUND.getMessage()));

    // nanaTitle 찾아서
    NanaTitle nanaTitle = nanaTitleRepository.findNanaTitleByIdAndLanguage(nana.getId(),
            memberInfoDto.getLanguage())
        .orElseThrow(() -> new NotFoundException(ErrorCode.NANA_TITLE_NOT_FOUND.getMessage()));

    if (isSearch) {
      searchService.updateSearchVolumeV1(NANA, nanaTitle.getNana().getId());
    }

    // nanaTitle에 맞는 게시물 조회
    List<NanaContent> nanaContentList = nanaContentRepository.findAllByNanaTitleOrderByNumber(
        nanaTitle);

    List<NanaResponse.NanaDetail> nanaDetails = new ArrayList<>();

    boolean isPostInFavorite = favoriteService.isPostInFavorite(memberInfoDto.getMember(), NANA,
        nanaTitle.getNana().getId());
    for (NanaContent nanaContent : nanaContentList) {

      // TODO hashtag 기능 구현 시 꼭 수정하기!!!!
      List<String> tmp = new ArrayList<>();
      tmp.add("ex1");
      tmp.add("ex2");
      nanaDetails.add(
          NanaResponse.NanaDetail.builder()
              .number(nanaContent.getNumber())
              .subTitle(nanaContent.getSubTitle())
              .title(nanaContent.getTitle())
              .imageUrl(nanaContent.getImageFile().getOriginUrl())
              .content(nanaContent.getContent())
              .additionalInfoList(getAdditionalInfoFromNanaContentEntity(nanaContent))
              .hashtags(tmp)
              .build());

    }

    return NanaResponse.NanaDetailDto.builder()
        .originUrl(nanaTitle.getImageFile().getOriginUrl())
        .subHeading(nanaTitle.getSubHeading())
        .heading(nanaTitle.getHeading())
        .notice(nanaTitle.getNotice())
        .nanaDetails(nanaDetails)
        .isFavorite(isPostInFavorite)
        .build();

  }

  // nanaContent의 AdditionalInfo dto로 바꾸기
  public List<NanaResponse.NanaAdditionalInfo> getAdditionalInfoFromNanaContentEntity(
      NanaContent nanaContent) {
    Set<NanaAdditionalInfo> eachInfoList = nanaContent.getInfoList();

    // 순서 보장 위해 List 형으로 바꾸고
    List<NanaAdditionalInfo> nanaAdditionalInfos = new ArrayList<>(eachInfoList);

    //DTO 형태로 변환
    List<NanaResponse.NanaAdditionalInfo> result = new ArrayList<>();
    for (NanaAdditionalInfo info : nanaAdditionalInfos) {
      result.add(NanaResponse.NanaAdditionalInfo.builder()
          .infoKey(info.getInfoType().getDescription())
          .infoValue(info.getDescription())
          .build());
    }
    return result;
  }
}
