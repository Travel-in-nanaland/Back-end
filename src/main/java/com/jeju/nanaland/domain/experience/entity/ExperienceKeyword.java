package com.jeju.nanaland.domain.experience.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "experience_keyword",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "experienceTypeKeywordUnique",
            columnNames = {"experience_id", "experience_type_keyword"}
        )
    }
)
public class ExperienceKeyword extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  private Experience experience;

  @Enumerated(EnumType.STRING)
  private ExperienceTypeKeyword experienceTypeKeyword;
}
