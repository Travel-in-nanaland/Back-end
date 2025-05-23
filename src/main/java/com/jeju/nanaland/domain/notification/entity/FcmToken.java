package com.jeju.nanaland.domain.notification.entity;

import com.jeju.nanaland.domain.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "fcm_token",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "memberTokenUniqueConstraint",
            columnNames = {"member_id", "token"}
        )
    }
)
public class FcmToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @NotNull
  private String token;

  @NotNull
  private LocalDateTime timestamp;

  public void updateTimestampToNow() {
    timestamp = LocalDateTime.now();
  }
}
