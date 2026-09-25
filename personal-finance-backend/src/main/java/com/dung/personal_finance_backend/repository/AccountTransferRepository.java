package com.dung.personal_finance_backend.repository;

import com.dung.personal_finance_backend.entity.AccountTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface AccountTransferRepository extends JpaRepository<AccountTransfer, Long> {
    List<AccountTransfer> findByUserIdOrderByTransferDateDescCreatedAtDesc(Long userId);

    boolean existsByUserIdAndSourceAccountIdOrUserIdAndTargetAccountId(
            Long sourceUserId,
            Long sourceAccountId,
            Long targetUserId,
            Long targetAccountId
    );

    @Query("""
            select sum(t.amount) from AccountTransfer t
            where t.user.id = :userId and t.sourceAccount.id = :accountId
            """)
    BigDecimal sumOutgoing(@Param("userId") Long userId, @Param("accountId") Long accountId);

    @Query("""
            select sum(t.amount) from AccountTransfer t
            where t.user.id = :userId and t.targetAccount.id = :accountId
            """)
    BigDecimal sumIncoming(@Param("userId") Long userId, @Param("accountId") Long accountId);
}
