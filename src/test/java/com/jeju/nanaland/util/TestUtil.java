package com.jeju.nanaland.util;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import jakarta.persistence.EntityManager;

public class TestUtil {

  public static Category findCategory(EntityManager em, CategoryContent categoryContent) {
    String jpql = "SELECT c FROM Category c WHERE c.content = :content";
    return em.createQuery(jpql, Category.class)
        .setParameter("content", categoryContent)
        .getSingleResult();
  }

  public static Language findLanguage(EntityManager em, Locale locale) {
    String jpql = "SELECT l FROM Language l WHERE l.locale = :locale";
    return em.createQuery(jpql, Language.class)
        .setParameter("locale", locale)
        .getSingleResult();
  }

  // 현재 InitTestData에 의해서 korean1,korean2,chinese1 입력되어 있음
  public static Member findMemberByLanguage(EntityManager em, Language language, int i) {
    String nickname = "";
    String jpql = "SELECT m FROM Member m WHERE m.language = :language AND m.nickname = :nickname";

    // 추후 확장을 위해 else아닌 else-if 로 작성
    if (language.getLocale() == Locale.KOREAN) {
      nickname = "korean";
    } else if (language.getLocale() == Locale.CHINESE) {
      nickname = "chinese";
    }
    return em.createQuery(jpql, Member.class)
        .setParameter("language", language)
        .setParameter("nickname", nickname + i)
        .getSingleResult();
  }

  public static MemberTravelType findMemberTravelType(EntityManager em, TravelType travelType) {
    String jpql = "SELECT t FROM MemberTravelType t WHERE t.travelType = :travelType";
    return em.createQuery(jpql, MemberTravelType.class)
        .setParameter("travelType", travelType)
        .getSingleResult();
  }

  public static ImageFile findImageFileByNumber(EntityManager em, int i) {
    String jpql = "SELECT i FROM ImageFile i WHERE i.originUrl = :originUrl";
    return em.createQuery(jpql, ImageFile.class)
        .setParameter("originUrl", "origin" + i)
        .getSingleResult();
  }

}
