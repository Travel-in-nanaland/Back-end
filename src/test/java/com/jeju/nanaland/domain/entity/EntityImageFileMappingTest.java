package com.jeju.nanaland.domain.entity;


import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.hashtag.entity.Hashtag;
import com.jeju.nanaland.domain.hashtag.entity.Keyword;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.Provider;
import com.jeju.nanaland.domain.review.entity.Review;
import com.jeju.nanaland.domain.review.entity.ReviewImageFile;
import com.jeju.nanaland.domain.stay.entity.Stay;
import com.jeju.nanaland.domain.story.entity.Story;
import com.jeju.nanaland.domain.story.entity.StoryCategory;
import com.jeju.nanaland.domain.story.entity.StoryImageFile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class EntityImageFileMappingTest {

  @PersistenceContext
  EntityManager em;

  Language language;

  ImageFile imageFile1;

  ImageFile imageFile2;

  Member member;

  @BeforeEach
  void init() {
    language = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yyyy-mm-dd")
        .build();
    em.persist(language);

    imageFile1 = ImageFile.builder()
        .thumbnailUrl("thumbnail_url")
        .originUrl("origin_url")
        .build();
    imageFile2 = ImageFile.builder()
        .thumbnailUrl("thumbnail_url")
        .originUrl("origin_url")
        .build();
    em.persist(imageFile1);
    em.persist(imageFile2);

    member = Member.builder()
        .language(language)
        .profileImageFile(imageFile1)
        .email("email")
        .password("password")
        .nickname("nickname")
        .provider(Provider.KAKAO)
        .providerId(1L)
        .build();
    em.persist(member);
  }


  @Test
  void storyImageFileMapping() {
    Story story = Story.builder()
        .member(member)
        .title("title")
        .storyCategory(StoryCategory.DRINKS)
        .content("content")
        .build();
    em.persist(story);

    StoryImageFile storyImageFile1 = StoryImageFile.builder()
        .imageFile(imageFile1)
        .story(story)
        .build();
    StoryImageFile storyImageFile2 = StoryImageFile.builder()
        .imageFile(imageFile2)
        .story(story)
        .build();
    em.persist(storyImageFile1);
    em.persist(storyImageFile2);
  }

  @Test
  void reviewImageFileMapping() {
    Keyword keyword = Keyword.builder()
        .content("content")
        .build();
    em.persist(keyword);

    Category category = Category.builder()
        .content("content")
        .build();
    em.persist(category);

    Stay stay = Stay.builder()
        .imageFile(imageFile1)
        .price(2000)
        .contact("010")
        .homepage("homepage")
        .parking("parking")
        .ratingAvg(3.5f)
        .build();
    em.persist(stay);

    Hashtag hashtag = Hashtag.builder()
        .keyword(keyword)
        .category(category)
        .postId(stay.getId())
        .build();
    em.persist(hashtag);

    Review review = Review.builder()
        .member(member)
        .category(category)
        .postId(stay.getId())
        .title("title")
        .content("content")
        .rating(3.5f)
        .build();
    em.persist(review);

    ReviewImageFile reviewImageFile1 = ReviewImageFile.builder()
        .review(review)
        .imageFile(imageFile1)
        .build();
    ReviewImageFile reviewImageFile2 = ReviewImageFile.builder()
        .review(review)
        .imageFile(imageFile2)
        .build();
    em.persist(reviewImageFile1);
    em.persist(reviewImageFile2);
  }
}
