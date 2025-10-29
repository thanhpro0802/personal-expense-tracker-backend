package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    // Tìm tất cả ví mà người dùng là owner hoặc member
    @Query("SELECT DISTINCT w FROM Wallet w LEFT JOIN w.members m WHERE w.owner.id = :userId OR m.id = :userId")
    List<Wallet> findAllWalletsByUserId(@Param("userId") UUID userId);

    // Lấy ví với danh sách members (fetch eager để tránh N+1 query)
    @Query("SELECT w FROM Wallet w LEFT JOIN FETCH w.members WHERE w.id = :walletId")
    Optional<Wallet> findByIdWithMembers(@Param("walletId") UUID walletId);
}
