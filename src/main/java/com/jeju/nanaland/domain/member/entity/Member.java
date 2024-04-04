package com.jeju.nanaland.domain.member.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.common.entity.ImageFile;
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
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "language_id", nullable = false)
  private Language language;

  @NotBlank
  @Column(nullable = false, unique = true, updatable = false)
  private String email;

  @NotBlank
  @Column(nullable = false)
  private String password;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "image_file_id", nullable = false)
  private ImageFile profileImageFile;

  @NotBlank
  @Column(nullable = false, unique = true)
  private String nickname;

  private String description;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Provider provider;

  @Column(nullable = false)
  private Long providerId;

  @ElementCollection(targetClass = Role.class)
  @CollectionTable(name = "roles", joinColumns = @JoinColumn(name = "member_id"))
  @Enumerated(EnumType.STRING)
  private Set<Role> roleSet;

  @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
  private List<Favorite> favorites;

  @Builder
  public Member(Language language, String email, String password, ImageFile profileImageFile,
      String nickname, String description, Provider provider, Long providerId) {
    this.language = language;
    this.email = email;
    this.password = password;
    this.profileImageFile = profileImageFile;
    this.nickname = nickname;
    this.description = (description != null) ? description : "";
    this.provider = provider;
    this.providerId = providerId;
    this.roleSet = new HashSet<>(List.of(Role.ROLE_MEMBER));
    this.favorites = new ArrayList<>();
  }
}
