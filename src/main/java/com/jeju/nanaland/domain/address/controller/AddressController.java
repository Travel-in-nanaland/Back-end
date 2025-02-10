package com.jeju.nanaland.domain.address.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.GET_KOREAN_ADDRESS_SUCCESS;

import com.jeju.nanaland.domain.address.service.AddressService;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.global.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/address")
@Slf4j
@Tag(name = "주소(address)")
public class AddressController {

  private final AddressService addressService;

  /**
   * 한국어 주소 제공 API
   *
   * @param postId   게시물 ID
   * @param category 게시물 카테고리
   * @param number   NANA_CONTENT 식별을 위한 파라미터. category가 NANA일 때만 사용
   * @return 한국어 주소
   */
  @GetMapping("/kr")
  public BaseResponse<String> getKoreanAddress(
      @RequestParam Long postId,
      @RequestParam Category category,
      @RequestParam(required = false) Long number) {

    return BaseResponse.success(GET_KOREAN_ADDRESS_SUCCESS,
        addressService.getKoreanAddress(postId, category, number));
  }
}
