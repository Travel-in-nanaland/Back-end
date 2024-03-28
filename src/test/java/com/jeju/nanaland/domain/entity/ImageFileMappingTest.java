package com.jeju.nanaland.domain.entity;


import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.story.entity.Story;
import com.jeju.nanaland.domain.story.entity.StoryCategory;
import com.jeju.nanaland.domain.story.entity.StoryImageFile;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Commit
public class ImageFileMappingTest {

  @Autowired
  EntityManager em;

  Language language;

  Member member;

  ImageFile imageFile1;

  ImageFile imageFile2;

  ImageFile imageFile3;

  @BeforeEach
  void init() {
    language = Language.builder()
        .locale("kr")
        .dateFormat("yyyy-mm-dd")
        .build();
    em.persist(language);

    member = Member.builder()
        .language(language)
        .email("email")
        .password("password")
        .nickname("nickname")
        .build();
    em.persist(member);

    imageFile1 = ImageFile.builder()
        .thumbnailUrl("thumbnail_url")
        .originUrl("origin_url")
        .build();
    imageFile2 = ImageFile.builder()
        .thumbnailUrl("thumbnail_url")
        .originUrl("origin_url")
        .build();
    imageFile3 = ImageFile.builder()
        .thumbnailUrl("thumbnail_url")
        .originUrl("origin_url")
        .build();

    em.persist(imageFile1);
    em.persist(imageFile2);
    em.persist(imageFile3);
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
}
