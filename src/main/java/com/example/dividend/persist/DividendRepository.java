package com.example.dividend.persist;

import com.example.dividend.persist.entity.DividendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, Long> {
    List<DividendEntity> findAllByCompanyId(Long companyId); // DividenEntity 에 있는 companyId와 일치하는 정보 찾아줌.

    @Transactional
    void deleteAllByCompanyId(Long id);

    boolean existsByCompanyIdAndDate(Long companyId, LocalDateTime date);

}
