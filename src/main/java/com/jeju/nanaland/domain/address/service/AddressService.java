package com.jeju.nanaland.domain.address.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService {

  private final NatureRepository natureRepository;
  private final FestivalRepository festivalRepository;
  private final ExperienceRepository experienceRepository;
  private final MarketRepository marketRepository;
  private final RestaurantRepository restaurantRepository;
  private final NanaRepository nanaRepository;

  /**
   * postId에 해당하는 게시물의 주소 반환
   *
   * @param postId   게시물 ID
   * @param category 게시물 카테고리
   * @param number   category가 NANA일때 NANA_CONTENT의 priority
   * @return 한국어 주소
   * @throws IllegalArgumentException category가 NANA이면서 number가 null일 때
   * @throws BadRequestException      category로 NANA_CONTENT가 들어온 경우
   * @throws NotFoundException        해당 게시물이 존재하지 않거나 주소 정보가 없을 경우
   */
  public String getKoreanAddress(Long postId, Category category, Long number) {

    if (category == Category.NANA && number == null) {
      throw new IllegalArgumentException("NANA 게시물은 number 값이 필수입니다.");
    }

    Optional<String> krAddress = switch (category) {
      case NATURE -> natureRepository.findKoreanAddress(postId);
      case FESTIVAL -> festivalRepository.findKoreanAddress(postId);
      case EXPERIENCE -> experienceRepository.findKoreanAddress(postId);
      case MARKET -> marketRepository.findKoreanAddress(postId);
      case RESTAURANT -> restaurantRepository.findKoreanAddress(postId);
      case NANA -> nanaRepository.findKoreanAddress(postId, number);
      default -> throw new BadRequestException("잘못된 카테고리입니다.");  // NANA_CONTENT
    };

    return krAddress.orElseThrow(() -> new NotFoundException("해당 게시물 또는 주소정보가 존재하지 않습니다."));
  }
}
