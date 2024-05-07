package com.jeju.nanaland.domain.member.service;

import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.NICKNAME_DUPLICATE;
import static java.lang.String.format;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.common.repository.LanguageRepository;
import com.jeju.nanaland.domain.member.dto.MemberRequest.JoinDto;
import com.jeju.nanaland.domain.member.dto.MemberRequest.LoginDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.Provider;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.auth.jwt.dto.JwtResponseDto.JwtDto;
import com.jeju.nanaland.global.exception.ConflictException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.jeju.nanaland.global.exception.UnauthorizedException;
import com.jeju.nanaland.global.image_upload.S3ImageService;
import com.jeju.nanaland.global.image_upload.dto.S3ImageDto;
import com.jeju.nanaland.global.util.JwtUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemberLoginService {

  private final MemberRepository memberRepository;
  private final LanguageRepository languageRepository;
  private final ImageFileRepository imageFileRepository;
  private final JwtUtil jwtUtil;
  private final MemberConsentService memberConsentService;
  private final S3ImageService s3ImageService;
  private final List<String> defaultProfile = Arrays.asList("LightPurple.png", "LightGray.png",
      "Gray.png", "DeepBlue.png");
  private final Random random = new Random();

  public void join(JoinDto joinDto, MultipartFile multipartFile) {
    /**
     * TODO : 회원 가입
     * TODO : JWT 발급
     */
    validateNickname(joinDto.getNickname());
    ImageFile profileImageFile = getProfileImageFile(multipartFile);
  }

  private void validateNickname(String nickname) {
    /**
     * TODO : 닉네임 글자 제한 확인
     */
    Optional<Member> memberOptional = memberRepository.findByNickname(nickname);
    if (memberOptional.isPresent()) {
      throw new ConflictException(NICKNAME_DUPLICATE.getMessage());
    }
  }

  private ImageFile getProfileImageFile(MultipartFile multipartFile) {
    S3ImageDto s3ImageDto;
    if (multipartFile == null) {
      s3ImageDto = getRandomProfileImageFile();
    } else {
      try {
        s3ImageDto = s3ImageService.uploadImageToS3(multipartFile, true);
      } catch (IOException e) {
        e.printStackTrace();
        throw new ServerErrorException(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
      }
    }
    
    ImageFile imageFile = ImageFile.builder()
        .originUrl(s3ImageDto.getOriginUrl())
        .thumbnailUrl(s3ImageDto.getThumbnailUrl())
        .build();
    return imageFileRepository.save(imageFile);
  }

  private S3ImageDto getRandomProfileImageFile() {
    String selectedProfile = defaultProfile.get(random.nextInt(defaultProfile.size()));
    return s3ImageService.getS3Urls(selectedProfile);
  }

  @Transactional
  public JwtDto login(LoginDto loginDto) {

    Member member = findLoginMember(loginDto);
    updateEmailDifferent(loginDto, member);

    return getJwtDto(member);
  }

  private JwtDto getJwtDto(Member member) {
    String accessToken = jwtUtil.getAccessToken(String.valueOf(member.getId()),
        member.getRoleSet());
    String refreshToken = jwtUtil.getRefreshToken(String.valueOf(member.getId()),
        member.getRoleSet());

    return JwtDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  private Member findLoginMember(LoginDto loginDto) {
    // 이메일, provider, providerId가 일치하는 회원 조회
    Optional<Member> memberOptional = memberRepository.findByEmailAndProviderAndProviderId(
        loginDto.getEmail(),
        Provider.valueOf(loginDto.getProvider()), loginDto.getProviderId());

    if (memberOptional.isPresent()) {
      return memberOptional.get();
    }

    // 해당 이메일로 가입된 계정이 없는 경우, 회원 가입 필요
    Member member = memberRepository.findByEmail(loginDto.getEmail())
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));

    // 해당 이메일로 가입된 계정이 있으나, provider과 providerId가 다른 경우, 소셜 로그인 변경 필요
    if (member.getProvider() != Provider.valueOf(loginDto.getProvider())
        || !member.getProviderId().equals(loginDto.getProviderId())) {
      throw new ConflictException(
          format(ErrorCode.CONFLICT_PROVIDER.getMessage(), member.getProvider()));
    }
    return member;
  }

//  private Member createMember(LoginDto loginDto) {
//
//    Optional<Member> memberOptional = memberRepository.findDuplicateMember(
//        loginDto.getEmail(),
//        Provider.valueOf(loginDto.getProvider()),
//        loginDto.getProviderId());
//
//    if (memberOptional.isPresent()) {
//      throw new ConflictException(ErrorCode.MEMBER_DUPLICATE.getMessage());
//    }
//
//    Language language = languageRepository.findByLocale(Locale.valueOf(loginDto.getLocale()));
//
//    ImageFile profileImageFile = getRandomProfileImageFile();
//
//    String nickname = loginDto.getProvider() + "_" + loginDto.getProviderId();
//
//    Member member = Member.builder()
//        .language(language)
//        .email(loginDto.getEmail())
//        .profileImageFile(profileImageFile)
//        .nickname(nickname)
//        .gender(loginDto.getGender())
//        .birthDate(loginDto.getBirthDate())
//        .provider(Provider.valueOf(loginDto.getProvider()))
//        .providerId(loginDto.getProviderId())
//        .build();
//    return memberRepository.save(member);
//  }

  @Transactional
  public void updateEmailDifferent(LoginDto loginDto, Member member) {
    if (!member.getEmail().equals(loginDto.getEmail())) {
      member.updateEmail(loginDto.getEmail());
    }
  }

  public JwtDto reissue(String bearerRefreshToken) {
    String refreshToken = jwtUtil.resolveToken(bearerRefreshToken);

    if (!jwtUtil.verifyRefreshToken(refreshToken)) {
      throw new UnauthorizedException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    String memberId = jwtUtil.getMemberIdFromRefresh(refreshToken);
    String savedRefreshToken = jwtUtil.findRefreshTokenById(memberId);

    if (!refreshToken.equals(savedRefreshToken)) {
      jwtUtil.deleteRefreshToken(memberId);
      throw new UnauthorizedException(ErrorCode.INVALID_TOKEN.getMessage());
    }

    Member member = memberRepository.findById(Long.valueOf(memberId))
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));

    return getJwtDto(member);
  }

  public void logout(MemberInfoDto memberInfoDto, String bearerAccessToken) {
    String accessToken = jwtUtil.resolveToken(bearerAccessToken);
    jwtUtil.setBlackList(accessToken);
    String memberId = String.valueOf(memberInfoDto.getMember().getId());
    if (jwtUtil.findRefreshTokenById(memberId) != null) {
      jwtUtil.deleteRefreshToken(memberId);
    }
  }
}
