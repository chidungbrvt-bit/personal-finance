package com.dung.personal_finance_backend.repository;

import com.dung.personal_finance_backend.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    public List<Account> findByUserId(Long userId);
    public Optional<Account> findByIdAndUserId(Long accountId, Long userId);
    List<Account> findByUserIdAndActiveTrue(Long userId);
    List<Account> findByUserIdAndActiveFalse(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :accountId and a.user.id = :userId")
    Optional<Account> findByIdAndUserIdForUpdate(
            @Param("accountId") Long accountId,
            @Param("userId") Long userId
    );
}
