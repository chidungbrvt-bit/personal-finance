package com.dung.personal_finance_backend.repository;

import com.dung.personal_finance_backend.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    public List<Account> findByUserId(Long userId);
    public Optional<Account> findByIdAndUserId(Long accountId, Long userId);
    List<Account> findByUserIdAndActiveTrue(Long userId);
    List<Account> findByUserIdAndActiveFalse(Long userId);
}
