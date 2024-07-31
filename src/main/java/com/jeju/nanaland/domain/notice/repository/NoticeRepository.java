package com.jeju.nanaland.domain.notice.repository;

import com.jeju.nanaland.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeRepositoryCustom {

}
