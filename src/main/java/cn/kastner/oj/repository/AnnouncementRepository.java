package cn.kastner.oj.repository;

import cn.kastner.oj.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AnnouncementRepository
    extends JpaRepository<Announcement, String>, JpaSpecificationExecutor<Announcement> {

  Optional<Announcement> findByTitle(String title);
}
