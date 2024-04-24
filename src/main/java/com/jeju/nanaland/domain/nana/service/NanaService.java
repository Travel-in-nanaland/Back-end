package com.jeju.nanaland.domain.nana.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.StatusDto;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnail;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailDto;
import com.jeju.nanaland.domain.nana.entity.NanaAdditionalInfo;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nana.repository.NanaContentRepository;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nana.repository.NanaTitleRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NanaService {

  private final NanaRepository nanaRepository;
  private final NanaTitleRepository nanaTitleRepository;
  private final NanaContentRepository nanaContentRepository;
  private final FavoriteService favoriteService;

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
              .build());

    }
    return NanaThumbnailDto.builder()
        .totalElements(resultDto.getTotalElements())
        .data(thumbnails)
        .build();
  }

  //나나 상세 게시물
  public NanaResponse.NanaDetailDto getNanaDetail(Long id) {
    // nanaTitle 찾아서
    NanaTitle nanaTitle = nanaTitleRepository.findNanaTitleById(id)
        .orElseThrow(() -> new BadRequestException("존재하지 않는 Nana 컨텐츠 입니다."));

    // nanaTitle에 맞는 게시물 조회
    List<NanaContent> nanaContentList = nanaContentRepository.findAllByNanaTitleOrderByNumber(
        nanaTitle);

    List<NanaResponse.NanaDetail> nanaDetails = new ArrayList<>();

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
        .notice(nanaTitle.getNotice())
        .nanaDetails(nanaDetails)
        .build();

  }

  @Transactional
  public StatusDto toggleLikeStatus(MemberInfoDto memberInfoDto, Long postId) {
    nanaRepository.findById(postId)
        .orElseThrow(() -> new BadRequestException("해당 id의 나나스픽 게시물이 존재하지 않습니다."));

    Boolean status = favoriteService.toggleLikeStatus(memberInfoDto.getMember(),
        CategoryContent.NANA, postId);
    return FavoriteResponse.StatusDto.builder()
        .isFavorite(status)
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
