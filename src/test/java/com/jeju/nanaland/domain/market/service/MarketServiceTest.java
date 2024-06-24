package com.jeju.nanaland.domain.market.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.util.TestUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketServiceTest {

  @PersistenceContext
  EntityManager em;
  @InjectMocks
  MarketService marketService;
  @Mock
  FavoriteRepository favoriteRepository;

  Language language;
  Member member1, member2;
  MemberInfoDto memberInfoDto1, memberInfoDto2;

  Market market;
  Category category;

  @BeforeEach
  void init() {
    ImageFile imageFile1 = TestUtil.findImageFileByNumber(em, 1);

    ImageFile imageFile2 = TestUtil.findImageFileByNumber(em, 2);

    language = TestUtil.findLanguage(em, Locale.KOREAN);

    member1 = TestUtil.findMemberByLanguage(em, language, 1);

    member2 = TestUtil.findMemberByLanguage(em, language, 2);

    memberInfoDto1 = MemberInfoDto.builder()
        .language(language)
        .member(member1)
        .build();

    memberInfoDto2 = MemberInfoDto.builder()
        .language(language)
        .member(member2)
        .build();

    market = Market.builder()
        .imageFile(imageFile1)
        .build();
    em.persist(market);

    category = TestUtil.findCategory(em, CategoryContent.MARKET);
  }
}