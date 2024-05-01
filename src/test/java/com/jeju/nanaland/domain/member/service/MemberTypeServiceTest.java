package com.jeju.nanaland.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.dto.MemberRequest.UpdateTypeDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberType;
import com.jeju.nanaland.domain.member.entity.Provider;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberTypeServiceTest {

  @Autowired
  EntityManager em;
  @Autowired
  MemberTypeService memberTypeService;
  @Autowired
  MemberRepository memberRepository;

  Language language;
  ImageFile imageFile;
  Member member;
  MemberInfoDto memberInfoDto;

  @BeforeEach
  void init() {
    language = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yyyy-mm-dd")
        .build();
    em.persist(language);

    imageFile = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    em.persist(imageFile);

    member = Member.builder()
        .email("test@naver.com")
        .provider(Provider.KAKAO)
        .providerId(123456789L)
        .nickname("nickname1")
        .language(language)
        .profileImageFile(imageFile)
        .build();
    em.persist(member);

    memberInfoDto = MemberInfoDto.builder()
        .language(language)
        .member(member)
        .build();
  }

  @DisplayName("사용자 타입 갱신")
  @Test
  void updateMemberTypeTest() {
    /**
     * GIVEN
     */
    UpdateTypeDto updateTypeDto = new UpdateTypeDto();
    updateTypeDto.setType(MemberType.GAMGYUL_SIKHYE.name());

    /**
     * WHEN
     */
    memberTypeService.updateMemberType(memberInfoDto, updateTypeDto);
    Member ResultDto = memberRepository.findById(member.getId()).get();

    /**
     * THEN
     */
    assertThat(ResultDto.getType()).isEqualTo(MemberType.GAMGYUL_SIKHYE);
  }
}