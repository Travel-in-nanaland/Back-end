package com.jeju.nanaland.domain.address.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
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

  public String getKoreanAddress(Long postId, Category category, Long number) {
    return switch (category) {
      case NATURE -> natureRepository.findKoreanAddress(postId);
      case FESTIVAL -> festivalRepository.findKoreanAddress(postId);
      case EXPERIENCE -> experienceRepository.findKoreanAddress(postId);
      case MARKET -> marketRepository.findKoreanAddress(postId);
      case RESTAURANT -> restaurantRepository.findKoreanAddress(postId);
      case NANA -> nanaRepository.findKoreanAddress(postId, number);
      default -> {
        throw new BadRequestException("잘못된 카테고리입니다.");
      }
    };
  }
}
