package com.jeju.nanaland.util;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.experience.entity.ExperienceTrans;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.festival.entity.FestivalTrans;
import com.jeju.nanaland.domain.market.entity.Market;
import com.jeju.nanaland.domain.market.entity.MarketTrans;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.domain.nature.entity.NatureTrans;
import jakarta.persistence.EntityManager;
import java.util.List;

public class TestUtil {

  // 언어이름+숫자로 데이터 입력 ex) korean1, korean2, chinese1, chinese2
  // 현재 InitTestData에 의해서 korean1,korean2,chinese1만 입력되어 있음 필요시 추가
  public static Member findMemberByLanguage(EntityManager em, Language language, int i) {
    String nickname = "";
    String jpql = "SELECT m FROM Member m WHERE m.language = :language AND m.nickname = :nickname";

    // 추후 확장을 위해 else아닌 else-if 로 작성
    if (language == Language.KOREAN) {
      nickname = "korean";
    } else if (language == Language.CHINESE) {
      nickname = "chinese";
    }
    return em.createQuery(jpql, Member.class)
        .setParameter("language", language)
        .setParameter("nickname", nickname + i)
        .getSingleResult();
  }

  // originUrl 필드를 origin + 숫자로 통일 킴. ex) Imagefile3 의 originUrl은 origin3
  public static ImageFile findImageFileByNumber(EntityManager em, int i) {
    String jpql = "SELECT i FROM ImageFile i WHERE i.originUrl = :originUrl";
    return em.createQuery(jpql, ImageFile.class)
        .setParameter("originUrl", "origin" + i)
        .getSingleResult();
  }

  // experience는 구분할 필드가 없어서 count 만큼 반환
  public static List<Experience> findExperienceList(EntityManager em, int count) {
    String jpql = "SELECT e FROM Experience e";
    return em.createQuery(jpql, Experience.class)
        .setMaxResults(count)
        .getResultList();
  }

  public static ExperienceTrans findExperienceTransByExperience(EntityManager em,
      Experience experience) {
    String jpql = "SELECT e FROM ExperienceTrans e WHERE e.experience = :experience";
    return em.createQuery(jpql, ExperienceTrans.class)
        .setParameter("experience", experience)
        .getSingleResult();
  }

  // 계절 띄어 쓰기 없이 작성되어있음. ex) 봄 / 여름,가을
  public static Festival findFestivalByStringSeason(EntityManager em, String season) {
    String jpql = "SELECT f FROM Festival f WHERE f.season = :season";
    return em.createQuery(jpql, Festival.class)
        .setParameter("season", season)
        .getSingleResult();
  }

  public static FestivalTrans findFestivalTransByFestival(EntityManager em, Festival festival) {
    String jpql = "SELECT f FROM FestivalTrans f WHERE f.festival = : festival";
    return em.createQuery(jpql, FestivalTrans.class)
        .setParameter("festival", festival)
        .getSingleResult();
  }

  // market은 구분할 필드가 없어서 count 만큼 반환
  public static List<Market> findMarketList(EntityManager em, int count) {
    String jpql = "SELECT m FROM Market m";
    return em.createQuery(jpql, Market.class)
        .setMaxResults(count)
        .getResultList();
  }

  public static MarketTrans findMarketTransByMarket(EntityManager em,
      Market market) {
    String jpql = "SELECT m FROM MarketTrans m WHERE m.market = :market";
    return em.createQuery(jpql, MarketTrans.class)
        .setParameter("market", market)
        .getSingleResult();
  }

  // nature는 구분할 필드가 없어서 count 만큼 반환
  public static List<Nature> findNatureList(EntityManager em, int count) {
    String jpql = "SELECT n FROM Nature n";
    return em.createQuery(jpql, Nature.class)
        .setMaxResults(count)
        .getResultList();
  }

  public static NatureTrans findNatureTransByNature(EntityManager em,
      Nature nature) {
    String jpql = "SELECT n FROM NatureTrans n WHERE n.nature = :nature";
    return em.createQuery(jpql, NatureTrans.class)
        .setParameter("nature", nature)
        .getSingleResult();
  }

  // nana는 ver필드로 구분 ex) nana3 의 ver은 ver3
  public static Nana findNana(EntityManager em, int i) {
    String jpql = "SELECT n FROM Nana n WHERE n.version = :version";
    return em.createQuery(jpql, Nana.class)
        .setParameter("version", "ver" + i)
        .getSingleResult();
  }

  public static NanaTitle findNanaTitleByNana(EntityManager em, Nana nana) {
    String jpql = "SELECT n FROM NanaTitle n WHERE n.nana = :nana";
    return em.createQuery(jpql, NanaTitle.class)
        .setParameter("nana", nana)
        .getSingleResult();
  }

  public static NanaContent findNanaContentByNanaTitleAndNumber(EntityManager em,
      NanaTitle nanaTitle, int priority) {
    String jpql = "SELECT n FROM NanaContent n WHERE n.nanaTitle = :nanaTitle AND n.priority = :priority";
    return em.createQuery(jpql, NanaContent.class)
        .setParameter("nanaTitle", nanaTitle)
        .setParameter("priority", priority)
        .getSingleResult();
  }

}

