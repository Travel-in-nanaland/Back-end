package com.jeju.nanaland.domain.member.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Status;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.member.dto.MemberRequest.ProfileUpdateDto;
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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

  @Enumerated(value = EnumType.STRING)
  @Column(name = "status")
  private Status status = Status.ACTIVE;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "language_id", nullable = false)
  private Language language;

  @NotBlank
  @Email
  @Column(nullable = false)
  private String email;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "image_file_id", nullable = false)
  private ImageFile profileImageFile;

  @NotBlank
  @Column(nullable = false, unique = true)
  private String nickname;

  private String description;

  @Column(nullable = false)
  private Integer level;

  private String gender;
  private LocalDate birthDate;

  @NotNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Provider provider;

  @NotNull
  @Column(nullable = false)
  private Long providerId;

  @ElementCollection(targetClass = Role.class)
  @CollectionTable(name = "roles", joinColumns = @JoinColumn(name = "member_id"))
  @Enumerated(EnumType.STRING)
  private Set<Role> roleSet;

  @Enumerated(EnumType.STRING)
  private MemberType type;

  @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
  private List<Favorite> favorites;

  @Builder
  public Member(Language language, String email, ImageFile profileImageFile,
      String nickname, String description, String gender, LocalDate birthDate,
      Provider provider, Long providerId, MemberType type) {
    this.language = language;
    this.email = email;
    this.profileImageFile = profileImageFile;
    this.nickname = nickname;
    this.description = (description != null) ? description : "";
    this.level = 1;
    this.gender = (gender != null) ? gender : "";
    this.birthDate = birthDate;
    this.provider = provider;
    this.providerId = providerId;
    this.roleSet = (provider == Provider.GUEST) ? new HashSet<>(List.of(Role.ROLE_GUEST))
        : new HashSet<>(List.of(Role.ROLE_MEMBER));
    this.type = type;
    this.favorites = new ArrayList<>();
  }

  public void updateMemberType(MemberType type) {
    this.type = type;
  }

  public void updateProfile(ProfileUpdateDto profileUpdateDto) {
    // TODO: 닉네임 중복 확인 & 닉네임 글자수 제한
    this.nickname =
        profileUpdateDto.getNickname() != null ? profileUpdateDto.getNickname() : this.nickname;
    this.description =
        profileUpdateDto.getDescription() != null ? profileUpdateDto.getDescription()
            : this.description;
  }

  public void updateStatus(Status status) {
    this.status = status;
  }

  public void updateLanguage(Language language) {
    this.language = language;
  }

  public void updatePersonalInfo() {
    this.email = "INACTIVE@nanaland.com";
    this.nickname = UUID.randomUUID().toString().substring(0, 16);
    this.providerId = -1L;
    this.gender = "";
    this.birthDate = null;
  }
}
