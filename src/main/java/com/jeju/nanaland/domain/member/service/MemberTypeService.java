package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.member.dto.MemberResponseDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.nature.dto.NatureNatureTransDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ServerErrorException;
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

  @Transactional
  public void updateMemberType(Long memberId, String type) {

    Member member = memberRepository.findById(memberId).orElseThrow(BadRequestException::new);

    member.updateMemberType(MemberType.valueOf(type));
  }

  public List<MemberResponseDto.RecommendedPosts> getRecommendedPostsByType(Long memberId) {

    Member member = memberRepository.findById(memberId).orElseThrow(BadRequestException::new);

    MemberType type = member.getType();
    Language language = member.getLanguage();

    // memberType에 저장된 카테고리, Id를 통해 조회 결과를 DTO로 반환
    List<MemberResponseDto.RecommendedPosts> result = new ArrayList<>();
    result.add(
        getRecommendedPostDto(type.getPostCategory1(), type.getPostId1(), language.getLocale()));
    result.add(
        getRecommendedPostDto(type.getPostCategory2(), type.getPostId2(), language.getLocale()));

    return result;
  }

  private MemberResponseDto.RecommendedPosts getRecommendedPostDto(String category, Long id,
      String locale) {

    switch (category) {
      case "NATURE" -> {
        NatureNatureTransDto dto = natureRepository.findNatureNatureTransDtoByIdAndLocale(id,
            locale);
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
      default -> throw new ServerErrorException("해당 관광지 정보가 존재하지 않습니다.");
    }
  }
}
