package com.expensetracker.backend.service.specifications;

import com.expensetracker.backend.model.Transaction;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.UUID;

public class TransactionSpecifications {

    public static Specification<Transaction> withWalletId(UUID walletId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("wallet").get("id"), walletId);
    }

    public static Specification<Transaction> withType(String type) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<Transaction> withCategory(String category) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<Transaction> withDateFrom(LocalDate dateFrom) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateFrom);
    }

    public static Specification<Transaction> withDateTo(LocalDate dateTo) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateTo);
    }

    public static Specification<Transaction> withSearchText(String searchText) {
        return (root, query, criteriaBuilder) -> {
            String likePattern = "%" + searchText.toLowerCase() + "%";
            // Tìm kiếm trong cả 'title' và 'category'
            Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likePattern);
            Predicate categoryLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), likePattern);
            return criteriaBuilder.or(titleLike, categoryLike);
        };
    }
}