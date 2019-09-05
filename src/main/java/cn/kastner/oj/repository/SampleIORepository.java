package cn.kastner.oj.repository;

import cn.kastner.oj.domain.SampleIO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SampleIORepository
    extends JpaRepository<SampleIO, String>, JpaSpecificationExecutor<SampleIO> {
}
