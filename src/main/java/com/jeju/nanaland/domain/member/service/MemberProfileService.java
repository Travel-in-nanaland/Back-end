package com.jeju.nanaland.domain.member.service;

import static com.jeju.nanaland.global.exception.ErrorCode.NICKNAME_DUPLICATE;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.repository.LanguageRepository;
import com.jeju.nanaland.domain.member.dto.MemberRequest.LanguageUpdateDto;
import com.jeju.nanaland.domain.member.dto.MemberRequest.ProfileUpdateDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse;
import com.jeju.nanaland.domain.member.dto.MemberResponse.ConsentItemResponse;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.exception.ConflictException;
import com.jeju.nanaland.global.exception.ErrorCode;
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

  private final LanguageRepository languageRepository;

  private final S3ImageService s3ImageService;
  private final MemberRepository memberRepository;

  @Transactional
  public void updateProfile(MemberInfoDto memberInfoDto, ProfileUpdateDto profileUpdateDto,
      MultipartFile multipartFile) {

    validateNickname(profileUpdateDto.getNickname());
    Member member = memberInfoDto.getMember();
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
        throw new ServerErrorException(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
      }
    }

    member.updateProfile(profileUpdateDto);
  }

  private void validateNickname(String nickname) {
    Optional<Member> memberOptional = memberRepository.findByNickname(nickname);
    if (memberOptional.isPresent()) {
      throw new ConflictException(NICKNAME_DUPLICATE.getMessage());
    }
  }

  public MemberResponse.ProfileDto getMemberProfile(MemberInfoDto memberInfoDto) {

    Member member = memberInfoDto.getMember();
    TravelType travelType = member.getMemberTravelType().getTravelType();
    Locale locale = member.getLanguage().getLocale();
    String typeName = travelType.getTypeNameWithLocale(locale);
    List<String> hashtags = new ArrayList<>();
    if (travelType != TravelType.NONE) {
      hashtags = travelType.getHashtagsWithLocale(locale);
    }

    List<ConsentItemResponse> consentItemResponses = memberRepository.findMemberConsentByMember(
        member).stream().map(memberConsent -> ConsentItemResponse.builder()
        .consentType(memberConsent.getConsentType().name())
        .consent(memberConsent.getConsent())
        .build()).toList();

    return MemberResponse.ProfileDto.builder()
        .email(member.getEmail())
        .provider(member.getProvider().name())
        .profileImageUrl(member.getProfileImageFile().getThumbnailUrl())
        .nickname(member.getNickname())
        .description(member.getDescription())
        .level(member.getLevel())
        .travelType(typeName)
        .hashtags(hashtags)
        .consentItems(consentItemResponses)
        .build();
  }

  @Transactional
  public void updateLanguage(MemberInfoDto memberInfoDto, LanguageUpdateDto languageUpdateDto) {
    Language locale = languageRepository.findByLocale(
        Locale.valueOf(languageUpdateDto.getLocale()));

    memberInfoDto.getMember().updateLanguage(locale);
  }
}
