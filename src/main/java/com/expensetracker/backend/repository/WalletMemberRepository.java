package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.WalletMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletMemberRepository extends JpaRepository<WalletMember, UUID> {

    @Query("SELECT wm FROM WalletMember wm WHERE wm.wallet.id = :walletId AND wm.user.id = :userId")
    Optional<WalletMember> findByWalletIdAndUserId(@Param("walletId") UUID walletId, @Param("userId") UUID userId);

    @Query("SELECT wm FROM WalletMember wm WHERE wm.wallet.id = :walletId")
    List<WalletMember> findAllByWalletId(@Param("walletId") UUID walletId);

    @Query("SELECT CASE WHEN COUNT(wm) > 0 THEN true ELSE false END FROM WalletMember wm WHERE wm.wallet.id = :walletId AND wm.user.id = :userId")
    boolean existsByWalletIdAndUserId(@Param("walletId") UUID walletId, @Param("userId") UUID userId);
}
