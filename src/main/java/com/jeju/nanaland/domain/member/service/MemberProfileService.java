package com.jeju.nanaland.domain.member.service;

import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.NICKNAME_DUPLICATE;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.dto.MemberRequest;
import com.jeju.nanaland.domain.member.dto.MemberResponse;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.exception.ConflictException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.jeju.nanaland.global.image_upload.S3ImageService;
import com.jeju.nanaland.global.image_upload.dto.S3ImageDto;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberProfileService {

  private final S3ImageService s3ImageService;
  private final MemberRepository memberRepository;

  /**
   * 유저 프로필 수정
   *
   * @param memberInfoDto 회원 정보
   * @param profileUpdateDto 프로필 수정 정보
   * @param multipartFile 프로필 사진
   * @throws ServerErrorException 사진 업로드가 실패했을 경우
   */
  @Transactional
  public void updateProfile(MemberInfoDto memberInfoDto,
      MemberRequest.ProfileUpdateDto profileUpdateDto, MultipartFile multipartFile) {

    Member member = memberInfoDto.getMember();
    validateNickname(profileUpdateDto.getNickname(), member);
    ImageFile profileImageFile = member.getProfileImageFile();
    if (multipartFile != null) {
      try {
        S3ImageDto s3ImageDto = s3ImageService.uploadOriginImageToS3(multipartFile, true);
        if (!s3ImageService.isDefaultProfileImage(profileImageFile)) {
          s3ImageService.deleteImageS3(profileImageFile);
        }
        profileImageFile.updateImageFile(s3ImageDto);
      } catch (IOException e) {
        log.error("S3 image upload error : {}", e.getMessage());
        throw new ServerErrorException(ErrorCode.SERVER_ERROR.getMessage());
      }
    }

    member.updateProfile(profileUpdateDto);
  }

  /**
   * 닉네임 중복 확인
   *
   * @param nickname 닉네임
   * @param member 회원
   * @throws ConflictException 닉네임이 중복되는 경우
   */
  public void validateNickname(String nickname, Member member) {
    Optional<Member> savedMember = memberRepository.findByNickname(nickname);
    if (savedMember.isPresent() && !savedMember.get().equals(member)) {
      throw new ConflictException(NICKNAME_DUPLICATE.getMessage());
    }
  }

  /**
   * 유저 프로필 조회
   * memberId가 본인과 일치하지 않으면, 타인 프로필 조회
   * 기본 프로필 정보 조회
   * 본인 프로필을 조회하는 경우, 이용약관 추가 조회
   * @param memberInfoDto 회원 정보
   * @param memberId 조회한 회원의 ID
   * @return 프로필 정보
   * @throws NotFoundException 존재하는 회원이 없는 경우
   */
  public MemberResponse.ProfileDto getMemberProfile(MemberInfoDto memberInfoDto, Long memberId) {
    // memberId가 본인과 일치하지 않는다면, 타인 프로필 조회
    Member member = memberInfoDto.getMember();
    boolean isMyProfile = true;
    if (memberId != null) {
      isMyProfile = member.getId().equals(memberId);
      if (!isMyProfile) {
        member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
      }
    }

    // 해시태그 조회
    TravelType travelType = member.getTravelType();
    Language language = member.getLanguage();
    String typeName = travelType.getTypeNameWithLocale(language);
    List<String> hashtags = new ArrayList<>();
    if (travelType != TravelType.NONE) {
      hashtags = travelType.getHashtagsWithLanguage(language);
    }

    // 이용약관 동의 여부 조회
    List<MemberResponse.ConsentItem> consentItems = new ArrayList<>();
    if (isMyProfile) {
      consentItems = memberRepository.findAllMemberConsent(member)
          .stream().map(
              memberConsent -> MemberResponse.ConsentItem.builder()
                  .consentType(memberConsent.getConsentType().name())
                  .consent(memberConsent.getConsent())
                  .build()
          ).toList();
    }

    return MemberResponse.ProfileDto.builder()
        .isMyProfile(isMyProfile)
        .consentItems(consentItems)
        .memberId(member.getId())
        .email(member.getEmail())
        .provider(member.getProvider().name())
        .profileImage(new ImageFileDto(
            member.getProfileImageFile().getOriginUrl(),
            member.getProfileImageFile().getThumbnailUrl()
        ))
        .nickname(member.getNickname())
        .description(member.getDescription())
        .travelType(typeName)
        .hashtags(hashtags)
        .build();
  }

  /**
   * 언어 설정 변경
   *
   * @param memberInfoDto 회원 정보
   * @param languageUpdateDto 언어 수정 정보
   */
  @Transactional
  public void updateLanguage(MemberInfoDto memberInfoDto,
      MemberRequest.LanguageUpdateDto languageUpdateDto) {
    Language language = Language.valueOf(languageUpdateDto.getLocale());

    memberInfoDto.getMember().updateLanguage(language);
  }

  // 닉네임 중복 확인

  /**
   * 닉네임 중복 확인
   * 본인 닉네임과 중복되는지 확인하기 위해 필요한 Member 조회 후, validate 과정 진행
   * @param nickname 닉네임
   * @param memberId 회원 ID
   */
  public void validateNickname(String nickname, Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));
    validateNickname(nickname, member);
  }
}
