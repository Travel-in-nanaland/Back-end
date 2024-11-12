package com.jeju.nanaland.domain.member.service;

import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_NOT_FOUND;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.image_upload.dto.S3ImageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileImageService {

  private final MemberRepository memberRepository;

  @Transactional
  public void updateMemberProfileImage(Long memberId, S3ImageDto s3ImageDto) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));
    ImageFile originImageFile = member.getProfileImageFile();
    originImageFile.updateImageFile(s3ImageDto.getOriginUrl(), s3ImageDto.getThumbnailUrl());
  }
}
