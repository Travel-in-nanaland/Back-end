package com.jeju.nanaland.domain.member.service;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
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

  @DisplayName("사용자 타입 갱신")
  @Test
  void updateMemberType() {
    /**
     * given
     */
    Language language = Language.builder()
        .locale("kr")
        .dateFormat("yyyy-mm-dd")
        .build();
    em.persist(language);

    ImageFile imageFile = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    em.persist(imageFile);

    Member member = Member.builder()
        .profileImageFile(imageFile)
        .language(language)
        .email("test@naver.com")
        .password("1234")
        .nickname("nickname")
        .build();
    em.persist(member);

    /**
     * when
     */
    String type = "GAMGYUL";
    memberTypeService.updateMemberType(member.getId(), type);
    Member result = memberRepository.findById(member.getId()).get();

    /**
     * then
     */
    Assertions.assertThat(result.getType()).isEqualTo(MemberType.GAMGYUL);
  }
}