package com.jeju.nanaland.domain.notification.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.notification.entity.FcmToken;
import java.util.List;

public interface FcmTokenRepositoryCustom {

  List<FcmToken> findAllByLanguage(Language language);
}
