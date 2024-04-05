package com.jeju.nanaland.domain.nature.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.nature.entity.QNature.nature;
import static com.jeju.nanaland.domain.nature.entity.QNatureTrans.natureTrans;

import com.jeju.nanaland.domain.nature.dto.NatureNatureTransDto;
import com.jeju.nanaland.domain.nature.dto.QNatureNatureTransDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class NatureRepositoryImplTest {

  @Autowired
  EntityManager em;

  @Autowired
  JPAQueryFactory queryFactory;

  @Test
  void queryTest() {
    Long languageId = 2L;
    Long id = 2L;

    NatureNatureTransDto result = queryFactory
        .select(new QNatureNatureTransDto(
            nature.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nature.contact,
            language.locale,
            natureTrans.title,
            natureTrans.content,
            natureTrans.address,
            natureTrans.intro,
            natureTrans.details,
            natureTrans.time,
            natureTrans.amenity
        ))
        .from(nature)
        .leftJoin(nature.imageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .where(nature.id.eq(id).and(natureTrans.language.id.eq(languageId)))
        .fetchOne();

    System.out.println(result);
  }
}