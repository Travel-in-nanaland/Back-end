package com.jeju.nanaland.domain.member.entity;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.Status;
import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.member.dto.MemberRequest.ProfileUpdateDto;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.Role;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
@Table(
    name = "member",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "memberProviderProviderIdUnique",
            columnNames = {"provider", "provider_id"}
        )
    }
)
public class Member extends BaseEntity {

  @Enumerated(value = EnumType.STRING)
  @Column(name = "status")
  private Status status = Status.ACTIVE;

  @NotNull
  @Enumerated(EnumType.STRING)
  private Language language;

  @NotBlank
  @Pattern(
      regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
      message = "이메일 형식이 올바르지 않습니다.")
  @Column(nullable = false)
  private String email;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "image_file_id", nullable = false)
  private ImageFile profileImageFile;

  @NotBlank
  @Column(nullable = false, unique = true)
  private String nickname;

  private String description;
  private String gender;
  private LocalDate birthDate;

  @NotNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Provider provider;

  @NotBlank
  @Column(nullable = false)
  private String providerId;

  @ElementCollection(targetClass = Role.class)
  @CollectionTable(name = "roles", joinColumns = @JoinColumn(name = "member_id"))
  @Enumerated(EnumType.STRING)
  private Set<Role> roleSet;

  @NotNull
  @Enumerated(EnumType.STRING)
  private TravelType travelType = TravelType.NONE;

  @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
  private List<Favorite> favorites;

  @Builder
  public Member(Language language, String email, ImageFile profileImageFile,
      String nickname, String description, String gender, LocalDate birthDate,
      Provider provider, String providerId, TravelType travelType) {
    this.language = language;
    this.email = email;
    this.profileImageFile = profileImageFile;
    this.nickname = nickname;
    this.description = (description != null) ? description : "";
    this.gender = (gender != null) ? gender : "";
    this.birthDate = birthDate;
    this.provider = provider;
    this.providerId = providerId;
    this.roleSet = (provider == Provider.GUEST) ? new HashSet<>(List.of(Role.ROLE_GUEST))
        : new HashSet<>(List.of(Role.ROLE_MEMBER));
    this.travelType = travelType;
    this.favorites = new ArrayList<>();
  }

  public void updateTravelType(TravelType travelType) {

    this.travelType = travelType;
  }

  public void updateProfile(ProfileUpdateDto profileUpdateDto) {
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
    this.providerId = "INACTIVE";
    this.gender = "";
    this.birthDate = null;
  }
}
