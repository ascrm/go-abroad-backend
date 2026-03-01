package com.goAbroad.auth.repository;

import com.goAbroad.auth.entity.UserSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 第三方登录 Repository
 */
@Repository
public interface UserSocialRepository extends JpaRepository<UserSocial, Long> {

    /**
     * 根据平台类型和openid查询
     */
    Optional<UserSocial> findBySocialTypeAndOpenid(Integer socialType, String openid);

    /**
     * 根据用户ID查询第三方登录列表
     */
    List<UserSocial> findByUserId(Long userId);
}
