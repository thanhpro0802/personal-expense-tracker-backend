package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Query("SELECT w FROM Wallet w JOIN w.members m WHERE m.user.id = :userId")
    List<Wallet> findAllByUserId(@Param("userId") UUID userId);
}
