package com.goAbroad.auth.repository;

import com.goAbroad.auth.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 账号 Repository
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * 根据账号值和类型查询账号
     */
    Optional<UserAccount> findByAccountTypeAndAccountValue(Integer accountType, String accountValue);

    /**
     * 根据账号值查询
     */
    @Query("SELECT ua FROM UserAccount ua WHERE ua.accountValue = :accountValue")
    List<UserAccount> findByAccountValue(@Param("accountValue") String accountValue);

    /**
     * 根据用户ID查询所有账号
     */
    List<UserAccount> findByUserId(Long userId);
}
