package com.goAbroad.auth.repository;

import com.goAbroad.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<User> findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCase(String username, String nickname, Pageable pageable);

    @Query(value = "SELECT * FROM tb_user WHERE status = 1 AND pgroonga_match_all(ARRAY[username, nickname]::text[], :keyword) ORDER BY pgroonga_score(ARRAY[username, nickname]::text[], :keyword) DESC LIMIT :limit", nativeQuery = true)
    List<User> searchByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);
}
