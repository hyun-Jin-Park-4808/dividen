package com.example.dividend.persist;

import com.example.dividend.model.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//MemberEntity 사용하기 위한 레파지토리

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findByUsername(String username);

    boolean existsByUsername(String username);

}
