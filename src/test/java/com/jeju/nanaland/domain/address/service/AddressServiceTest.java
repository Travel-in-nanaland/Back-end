package com.jeju.nanaland.domain.address.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
@Transactional
@Execution(ExecutionMode.CONCURRENT)
public class AddressServiceTest {

  @InjectMocks
  AddressService addressService;

  @Mock
  NatureRepository natureRepository;
  @Mock
  FestivalRepository festivalRepository;
  @Mock
  MarketRepository marketRepository;
  @Mock
  ExperienceRepository experienceRepository;
  @Mock
  RestaurantRepository restaurantRepository;
  @Mock
  NanaRepository nanaRepository;

  Map<Category, Function<Long, Optional<String>>> functionMap = new HashMap<>();

  void initFunctionMap() {
    functionMap.put(Category.NATURE, natureRepository::findKoreanAddress);
    functionMap.put(Category.MARKET, marketRepository::findKoreanAddress);
    functionMap.put(Category.FESTIVAL, festivalRepository::findKoreanAddress);
    functionMap.put(Category.EXPERIENCE, experienceRepository::findKoreanAddress);
    functionMap.put(Category.RESTAURANT, restaurantRepository::findKoreanAddress);
  }

  @DisplayName("한국어 주소 조회 - NANA, NANA_CONTENT 제외")
  @ParameterizedTest
  @EnumSource(value = Category.class, names = {"NANA", "NANA_CONTENT"}, mode = Mode.EXCLUDE)
  void getKoreanAddressExcludeNanaAndNanaContentTest(Category category) {
    // given
    initFunctionMap();
    when(functionMap.get(category).apply(1L)).thenReturn(Optional.of("한국어주소"));

    // when
    String result = addressService.getKoreanAddress(1L, category, null);

    // then
    assertThat(result).isEqualTo("한국어주소");
  }

  @DisplayName("한국어 주소 조회 - NANA")
  @Test
  void getKoreanAddressWithNanaTest() {
    // given
    Category category = Category.NANA;
    when(nanaRepository.findKoreanAddress(1L, 1L)).thenReturn(Optional.of("한국어주소"));

    // when
    String result = addressService.getKoreanAddress(1L, category, 1L);

    // then
    assertThat(result).isEqualTo("한국어주소");
  }

  @DisplayName("한국어 주소 조회 실패 - 나나스픽 게시물을 number 없이 요청한 경우")
  @Test
  void getKoreanAddressWithoutNumberTest() {
    // given
    Category category = Category.NANA;

    // when
    // then
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> addressService.getKoreanAddress(1L, category, null)
    );
  }

  @DisplayName("한국어 주소 조회 실패 - NANA_CONTENT를 통해 요청이 들어온 경우")
  @Test
  void getKoreanAddressWithNanaContentTest() {
    // given
    Category category = Category.NANA_CONTENT;

    // when
    // then
    Assertions.assertThrows(BadRequestException.class,
        () -> addressService.getKoreanAddress(1L, category, null)
    );
  }
}
