package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberRequest.UpdateTypeDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.member.repository.MemberTravelTypeRepository;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberTypeService {

  private final MemberRepository memberRepository;
  private final NatureRepository natureRepository;
  private final FestivalRepository festivalRepository;
  private final ExperienceRepository experienceRepository;
  private final MarketRepository marketRepository;
  private final MemberTravelTypeRepository memberTravelTypeRepository;

  @Transactional
  public void updateMemberType(MemberInfoDto memberInfoDto, UpdateTypeDto updateTypeDto) {

    Member member = memberInfoDto.getMember();
    String newTravelType = updateTypeDto.getType();
    MemberTravelType newType = memberTravelTypeRepository.findByTravelType(
        TravelType.valueOf(newTravelType));

    // Enum에는 있지만 DB에는 없는 경우
    if (newType == null) {
      String errorMessage = newTravelType + "에 해당하는 타입 정보가 없습니다.";
      log.error(errorMessage);
      throw new ServerErrorException(errorMessage);
    }

    member.updateMemberTravelType(newType);
  }

//  public List<MemberResponse.RecommendPostDto> getRecommendPostsByType(Long memberId) {
//
//    Member member = memberRepository.findById(memberId).orElseThrow(BadRequestException::new);
//
//    TravelType type = member.getMemberTravelType().getTravelType();
//    if (type == TravelType.NONE) {
//
//    }
//    com.jeju.nanaland.domain.common.entity.Locale locale = member.getLanguage().getLocale();
//
//    // memberType에 저장된 Pair<카테고리, Id>를 순회하며 DTO 추가
//    List<MemberResponse.RecommendPostDto> result = new ArrayList<>();
//    for (Pair<String, Long> recommendPost : type.getRecommendPosts()) {
//      result.add(getRecommendPostDto(recommendPost, locale));
//    }
//
//    return result;
//  }

  private MemberResponse.RecommendPostDto getRecommendPostDto(Pair<String, Long> recommendPost,
      Locale locale) {

    String category = recommendPost.getFirst();
    Long id = recommendPost.getSecond();
    switch (category) {
      case "NATURE" -> {
        NatureCompositeDto dto = natureRepository.findCompositeDtoById(id, locale);
        if (dto == null) {
          throw new ServerErrorException("해당 관광지 정보가 존재하지 않습니다.");
        }

        return MemberResponse.RecommendPostDto.builder()
            .id(dto.getId())
            .category(category)
            .thumbnailUrl(dto.getThumbnailUrl())
            .title(dto.getTitle())
            .intro(dto.getIntro())
            .build();
      }
      case "FESTIVAL" -> {
        FestivalCompositeDto dto = festivalRepository.findCompositeDtoById(id, locale);
        if (dto == null) {
          throw new ServerErrorException("해당 관광지 정보가 존재하지 않습니다.");
        }

        return MemberResponse.RecommendPostDto.builder()
            .id(dto.getId())
            .category(category)
            .thumbnailUrl(dto.getThumbnailUrl())
            .title(dto.getTitle())
            .intro(dto.getIntro())
            .build();
      }
      case "EXPERIENCE" -> {
        ExperienceCompositeDto dto = experienceRepository.findCompositeDtoById(id, locale);
        if (dto == null) {
          throw new ServerErrorException("해당 관광지 정보가 존재하지 않습니다.");
        }

        return MemberResponse.RecommendPostDto.builder()
            .id(dto.getId())
            .category(category)
            .thumbnailUrl(dto.getThumbnailUrl())
            .title(dto.getTitle())
            .intro(dto.getIntro())
            .build();
      }
      case "MARKET" -> {
        MarketCompositeDto dto = marketRepository.findCompositeDtoById(id, locale);
        if (dto == null) {
          throw new ServerErrorException("해당 관광지 정보가 존재하지 않습니다.");
        }

        return MemberResponse.RecommendPostDto.builder()
            .id(dto.getId())
            .category(category)
            .thumbnailUrl(dto.getThumbnailUrl())
            .title(dto.getTitle())
            .intro(dto.getIntro())
            .build();
      }
      default -> throw new ServerErrorException("해당 관광지 정보가 존재하지 않습니다.");
    }
  }
}
