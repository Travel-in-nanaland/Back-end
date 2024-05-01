package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
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
import com.jeju.nanaland.domain.member.entity.MemberType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.mysema.commons.lang.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

  @Transactional
  public void updateMemberType(MemberInfoDto memberInfoDto, UpdateTypeDto reqDto) {

    memberInfoDto.getMember().updateMemberType(MemberType.valueOf(reqDto.getType()));
  }

  public List<MemberResponse.RecommendPostDto> getRecommendPostsByType(
      MemberInfoDto memberInfoDto) {

    Member member = memberRepository.findById(memberInfoDto.getMember().getId())
        .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    MemberType type = member.getType();
    if (type == null) {
      type = getRandomMemberType();
    }
    Locale locale = member.getLanguage().getLocale();

    List<MemberResponse.RecommendPostDto> result = new ArrayList<>();
    for (Pair<CategoryContent, Long> recommendPost : type.getRecommendPosts()) {
      result.add(getRecommendPostDto(recommendPost, locale));
    }

    return result;
  }

  private MemberType getRandomMemberType() {
    Random random = new Random();
    MemberType[] memberTypes = MemberType.values();

    return memberTypes[random.nextInt(memberTypes.length)];
  }

  private MemberResponse.RecommendPostDto getRecommendPostDto(
      Pair<CategoryContent, Long> recommendPost,
      Locale locale) {

    CategoryContent categoryContent = recommendPost.getFirst();
    Long id = recommendPost.getSecond();
    switch (categoryContent) {
      case NATURE -> {
        NatureCompositeDto dto = natureRepository.findCompositeDtoById(id, locale);
        if (dto == null) {
          log.error("NATURE id: {} NOT FOUND", id);
          throw new NotFoundException("해당 7대자연 정보가 존재하지 않습니다.");
        }

        return MemberResponse.RecommendPostDto.builder()
            .id(dto.getId())
            .category(categoryContent.name())
            .thumbnailUrl(dto.getThumbnailUrl())
            .title(dto.getTitle())
            .intro(dto.getIntro())
            .build();
      }
      case FESTIVAL -> {
        FestivalCompositeDto dto = festivalRepository.findCompositeDtoById(id, locale);
        if (dto == null) {
          throw new NotFoundException("해당 축제 정보가 존재하지 않습니다.");
        }

        return MemberResponse.RecommendPostDto.builder()
            .id(dto.getId())
            .category(categoryContent.name())
            .thumbnailUrl(dto.getThumbnailUrl())
            .title(dto.getTitle())
            .intro(dto.getIntro())
            .build();
      }
      case EXPERIENCE -> {
        ExperienceCompositeDto dto = experienceRepository.findCompositeDtoById(id, locale);
        if (dto == null) {
          throw new NotFoundException("해당 이색체험 정보가 존재하지 않습니다.");
        }

        return MemberResponse.RecommendPostDto.builder()
            .id(dto.getId())
            .category(categoryContent.name())
            .thumbnailUrl(dto.getThumbnailUrl())
            .title(dto.getTitle())
            .intro(dto.getIntro())
            .build();
      }
      case MARKET -> {
        MarketCompositeDto dto = marketRepository.findCompositeDtoById(id, locale);
        if (dto == null) {
          throw new NotFoundException("해당 관광지 정보가 존재하지 않습니다.");
        }

        return MemberResponse.RecommendPostDto.builder()
            .id(dto.getId())
            .category(categoryContent.name())
            .thumbnailUrl(dto.getThumbnailUrl())
            .title(dto.getTitle())
            .intro(dto.getIntro())
            .build();
      }
      default -> throw new NotFoundException("해당 카테고리 정보가 존재하지 않습니다.");
    }
  }
}
