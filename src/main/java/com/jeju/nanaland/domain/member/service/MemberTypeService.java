package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponseDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.mysema.commons.lang.Pair;
import java.util.ArrayList;
import java.util.List;
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
  public void updateMemberType(Long memberId, String type) {

    Member member = memberRepository.findById(memberId).orElseThrow(BadRequestException::new);

    member.updateMemberType(MemberType.valueOf(type));
  }

  public List<MemberResponseDto.RecommendedPosts> getRecommendedPostsByType(Long memberId) {

    Member member = memberRepository.findById(memberId).orElseThrow(BadRequestException::new);

    MemberType type = member.getType();
    String locale = member.getLanguage().getLocale();

    // memberType에 저장된 Pair<카테고리, Id>를 순회하며 DTO 추가
    List<MemberResponseDto.RecommendedPosts> result = new ArrayList<>();
    for (Pair<String, Long> recommendPost : type.getRecommendPosts()) {
      result.add(getRecommendedPostDto(recommendPost, locale));
    }

    return result;
  }

  private MemberResponseDto.RecommendedPosts getRecommendedPostDto(Pair<String, Long> recommendPost,
      String locale) {

    String category = recommendPost.getFirst();
    Long id = recommendPost.getSecond();
    switch (category) {
      case "NATURE" -> {
        NatureCompositeDto dto = natureRepository.findNatureCompositeDto(id, locale);
        if (dto == null) {
          throw new ServerErrorException("해당 관광지 정보가 존재하지 않습니다.");
        }

        return MemberResponseDto.RecommendedPosts.builder()
            .id(dto.getId())
            .category(category)
            .thumbnailUrl(dto.getThumbnailUrl())
            .title(dto.getTitle())
            .intro(dto.getIntro())
            .build();
      }
      case "FESTIVAL" -> {
        FestivalCompositeDto dto = festivalRepository.findFestivalCompositeDto(id, locale);
        if (dto == null) {
          throw new ServerErrorException("해당 관광지 정보가 존재하지 않습니다.");
        }

        return MemberResponseDto.RecommendedPosts.builder()
            .id(dto.getId())
            .category(category)
            .thumbnailUrl(dto.getThumbnailUrl())
            .title(dto.getTitle())
            // intro 필드 없음
            .build();
      }
      case "EXPERIENCE" -> {
        ExperienceCompositeDto dto = experienceRepository.findExperienceCompositeDto(id, locale);
        if (dto == null) {
          throw new ServerErrorException("해당 관광지 정보가 존재하지 않습니다.");
        }

        return MemberResponseDto.RecommendedPosts.builder()
            .id(dto.getId())
            .category(category)
            .thumbnailUrl(dto.getThumbnailUrl())
            .title(dto.getTitle())
            .intro(dto.getIntro())
            .build();
      }
      case "MARKET" -> {
        MarketCompositeDto dto = marketRepository.findMarketCompositeDto(id, locale);
        if (dto == null) {
          throw new ServerErrorException("해당 관광지 정보가 존재하지 않습니다.");
        }

        return MemberResponseDto.RecommendedPosts.builder()
            .id(dto.getId())
            .category(category)
            .thumbnailUrl(dto.getThumbnailUrl())
            .title(dto.getTitle())
            // intro 필드 없음
            .build();
      }
      default -> throw new ServerErrorException("해당 관광지 정보가 존재하지 않습니다.");
    }
  }
}
