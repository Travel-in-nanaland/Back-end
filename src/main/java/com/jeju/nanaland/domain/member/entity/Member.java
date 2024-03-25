package com.jeju.nanaland.domain.member.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "language_id", nullable = false)
  private Language language;

  @Column(nullable = false, unique = true, updatable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  private String profileUrl;

  @Column(nullable = false, unique = true)
  private String nickname;

  private String description;

  @ElementCollection(targetClass = Role.class)
  @CollectionTable(name = "roles", joinColumns = @JoinColumn(name = "member_id"))
  @Enumerated(EnumType.STRING)
  private Set<Role> roleSet;

  @Column(columnDefinition = "TEXT")
  private String accessToken;

  @Column(columnDefinition = "TEXT")
  private String refreshToken;

  @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
  private List<Favorite> favorites;

  @Builder
  public Member(Language language, String email, String password, String profileUrl,
      String nickname, String description) {
    this.language = language;
    this.email = email;
    this.password = password;
    this.profileUrl = (profileUrl != null) ? profileUrl : "";
    this.nickname = nickname;
    this.description = (description != null) ? description : "";
    this.roleSet = new HashSet<>(List.of(Role.ROLE_MEMBER));
    this.accessToken = "";
    this.refreshToken = "";
    this.favorites = new ArrayList<>();
  }
}
