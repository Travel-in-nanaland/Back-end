package com.jeju.nanaland.domain.favorite.entity;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
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
    name = "favorite",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "memberCategoryPostUnique",
            columnNames = {"member_id", "category", "post_id"}
        )
    }
)
public class Favorite extends BaseEntity {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @NotNull
  @Enumerated(EnumType.STRING)
  private Category category;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @Column(nullable = false, columnDefinition = "TINYINT default 0")
  private int notificationCount;  // 알림 전송 횟수

  @Column(nullable = false, columnDefinition = "VARCHAR(8) default 'ACTIVE'")
  private String status;

  public boolean isStatusActive() {
    return this.status.equals("ACTIVE");
  }

  public void setStatusActive() {
    this.status = "ACTIVE";
  }

  public void setStatusInactive() {
    this.status = "INACTIVE";
  }
}
