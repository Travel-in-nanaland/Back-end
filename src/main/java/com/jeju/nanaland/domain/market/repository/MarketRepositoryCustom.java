package com.jeju.nanaland.domain.market.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnail;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MarketRepositoryCustom {

  MarketCompositeDto findCompositeDtoById(Long id, Language locale);

  Page<MarketThumbnail> findMarketThumbnails(Language locale, List<String> addressFilterList,
      Pageable pageable);

  Page<MarketCompositeDto> searchCompositeDtoByKeyword(String keyword, Language locale,
      Pageable pageable);
}
