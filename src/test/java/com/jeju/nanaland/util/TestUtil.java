package com.jeju.nanaland.util;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
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

  public static MemberTravelType findMemberTravelType(EntityManager em, TravelType travelType) {
    String jpql = "SELECT t FROM MemberTravelType t WHERE t.travelType = :travelType";
    return em.createQuery(jpql, MemberTravelType.class)
        .setParameter("travelType", travelType)
        .getSingleResult();
  }


}
